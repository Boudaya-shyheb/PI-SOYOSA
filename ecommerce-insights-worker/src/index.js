require('dotenv').config();

const { createPool } = require('./db');
const { fetchOpenAlex } = require('./clients/openAlex');
const { fetchCrossref } = require('./clients/crossref');
const cron = require('node-cron');
const fs = require('fs');

const BATCH_SIZE = Number(process.env.BATCH_SIZE || 20);
const SLEEP_MS = Number(process.env.SLEEP_MS || 1200);
const RETRY_LIMIT = Number(process.env.RETRY_LIMIT || 3);
const RETRY_BASE_MS = Number(process.env.RETRY_BASE_MS || 1000);
const COOLDOWN_MS = Number(process.env.COOLDOWN_MS || 15000);
const MAX_CONSECUTIVE_429 = Number(process.env.MAX_CONSECUTIVE_429 || 5);
const OPENALEX_ENABLED = process.env.OPENALEX_ENABLED === undefined
  ? true
  : String(process.env.OPENALEX_ENABLED).toLowerCase() === 'true';
const CROSSREF_ENABLED = process.env.CROSSREF_ENABLED === undefined
  ? true
  : String(process.env.CROSSREF_ENABLED).toLowerCase() === 'true';

let consecutive429s = 0;
const DRY_RUN = String(process.env.DRY_RUN || '').toLowerCase() === 'true';
const CRON_ENABLED = String(process.env.CRON_ENABLED || '').toLowerCase() === 'true';
const CRON_SCHEDULE = process.env.CRON_SCHEDULE || '0 2 * * 1';
const RUN_ON_START = process.env.RUN_ON_START === undefined
  ? true
  : String(process.env.RUN_ON_START).toLowerCase() === 'true';
const LOG_FILE = process.env.LOG_FILE || '';

function logMessage(message) {
  const line = `[${new Date().toISOString()}] ${message}`;
  console.log(line);
  if (LOG_FILE) {
    try {
      fs.appendFileSync(LOG_FILE, `${line}\n`);
    } catch (error) {
      console.error(`Failed to write log file: ${error?.message || error}`);
    }
  }
}

function sleep(ms) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

function normalizeIsbn(raw) {
  if (!raw) {
    return null;
  }
  const trimmed = String(raw).trim();
  if (!trimmed) {
    return null;
  }
  return trimmed.replace(/[\s-]/g, '').toUpperCase();
}

async function fetchCandidates(pool, offset) {
  const [rows] = await pool.query(
    `SELECT p.id, p.name, p.isbn, c.name AS category
     FROM products p
     JOIN categories c ON c.id = p.category_id
     WHERE (c.name = 'Books' OR p.isbn IS NOT NULL)
     ORDER BY p.id
     LIMIT ? OFFSET ?`,
    [BATCH_SIZE, offset]
  );
  return rows.map((row) => ({
    id: row.id,
    name: row.name,
    isbn: normalizeIsbn(row.isbn),
    category: row.category
  }));
}

async function upsertInsight(pool, productId, insight) {
  if (DRY_RUN) {
    logMessage(`[dry-run] Would upsert ${insight.source} insight for product ${productId}`);
    return;
  }
  await pool.query(
    `INSERT INTO external_product_insights (
      product_id,
      source,
      source_id,
      isbn,
      title,
      authors,
      publisher,
      published_date,
      page_count,
      language,
      categories,
      average_rating,
      ratings_count,
      thumbnail_url,
      info_link,
      raw_payload,
      created_at,
      updated_at
    ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())
    ON DUPLICATE KEY UPDATE
      source_id = VALUES(source_id),
      isbn = VALUES(isbn),
      title = VALUES(title),
      authors = VALUES(authors),
      publisher = VALUES(publisher),
      published_date = VALUES(published_date),
      page_count = VALUES(page_count),
      language = VALUES(language),
      categories = VALUES(categories),
      average_rating = VALUES(average_rating),
      ratings_count = VALUES(ratings_count),
      thumbnail_url = VALUES(thumbnail_url),
      info_link = VALUES(info_link),
      raw_payload = VALUES(raw_payload),
      updated_at = NOW()`,
    [
      productId,
      insight.source,
      insight.sourceId,
      insight.isbn,
      insight.title,
      insight.authors,
      insight.publisher,
      insight.publishedDate,
      insight.pageCount,
      insight.language,
      insight.categories,
      insight.averageRating,
      insight.ratingsCount,
      insight.thumbnailUrl,
      insight.infoLink,
      insight.rawPayload
    ]
  );
}

async function processProduct(pool, product) {
  if (OPENALEX_ENABLED) {
    const openAlexInsight = await withRetry(() => fetchOpenAlex(product));
    if (openAlexInsight) {
      await upsertInsight(pool, product.id, openAlexInsight);
    }
    await sleep(SLEEP_MS);
  }

  if (CROSSREF_ENABLED) {
    const crossrefInsight = await withRetry(() => fetchCrossref(product));
    if (crossrefInsight) {
      await upsertInsight(pool, product.id, crossrefInsight);
    }
  }
}

async function withRetry(task) {
  let attempt = 0;
  const retryStatuses = new Set([429, 503, 502, 504, 408]);
  while (attempt <= RETRY_LIMIT) {
    try {
      const result = await task();
      consecutive429s = 0;
      return result;
    } catch (error) {
      const status = error?.response?.status;
      const isTimeout = error?.code === 'ECONNABORTED' || String(error?.message || '').includes('timeout');
      const shouldRetry = (status && retryStatuses.has(status)) || isTimeout;
      if (!shouldRetry || attempt === RETRY_LIMIT) {
        throw error;
      }
      if (status === 429) {
        consecutive429s += 1;
      }
      const delay = RETRY_BASE_MS * Math.pow(2, attempt);
      logMessage(`Rate limited (429). Retrying in ${delay}ms...`);
      await sleep(delay);
      if (consecutive429s >= MAX_CONSECUTIVE_429) {
        logMessage(`Too many 429s. Cooling down for ${COOLDOWN_MS}ms...`);
        await sleep(COOLDOWN_MS);
        consecutive429s = 0;
      }
      attempt += 1;
    }
  }
  return null;
}

async function run() {
  const pool = await createPool();
  let offset = 0;
  let processed = 0;

  try {
    while (true) {
      const products = await fetchCandidates(pool, offset);
      if (!products.length) {
        break;
      }

      for (const product of products) {
        try {
          await processProduct(pool, product);
          processed += 1;
        } catch (error) {
          const message = error?.message || String(error);
          console.error(`Failed to process product ${product.id}: ${message}`);
        }
        await sleep(SLEEP_MS);
      }

      offset += BATCH_SIZE;
    }
  } finally {
    await pool.end();
    logMessage(`Worker finished. Processed ${processed} products.`);
  }
}

let isRunning = false;

async function safeRun() {
  if (isRunning) {
    logMessage('Worker already running, skipping this tick.');
    return;
  }
  isRunning = true;
  try {
    await run();
  } finally {
    isRunning = false;
  }
}

if (CRON_ENABLED) {
  if (!cron.validate(CRON_SCHEDULE)) {
    console.error(`Invalid CRON_SCHEDULE: ${CRON_SCHEDULE}`);
    process.exit(1);
  }
  cron.schedule(CRON_SCHEDULE, () => {
    safeRun().catch((error) => {
      const message = error?.message || String(error);
      console.error(`Scheduled run failed: ${message}`);
    });
  });
  logMessage(`Cron enabled. Schedule: ${CRON_SCHEDULE}`);
  if (RUN_ON_START) {
    safeRun().catch((error) => {
      const message = error?.message || String(error);
      logMessage(`Initial run failed: ${message}`);
      process.exit(1);
    });
  }
} else {
  safeRun().catch((error) => {
    const message = error?.message || String(error);
    logMessage(`Worker failed: ${message}`);
    process.exit(1);
  });
}
