const axios = require('axios');

const OPENALEX_API = 'https://api.openalex.org/works';
const REQUEST_TIMEOUT_MS = Number(process.env.REQUEST_TIMEOUT_MS || 20000);

function buildFilter(product) {
  if (product.isbn) {
    return `isbn:${product.isbn}`;
  }
  return `title.search:${product.name}`;
}

async function fetchOpenAlex(product) {
  const filter = buildFilter(product);
  const response = await axios.get(OPENALEX_API, {
    params: {
      filter,
      per_page: 1
    },
    timeout: REQUEST_TIMEOUT_MS,
    headers: {
      'User-Agent': 'ecommerce-insights-worker/1.0'
    }
  });

  const data = response.data || {};
  const results = data.results || [];
  if (results.length === 0) {
    return null;
  }

  const item = results[0];
  const authors = Array.isArray(item.authorships)
    ? item.authorships
      .map((auth) => auth.author?.display_name)
      .filter(Boolean)
      .join('; ')
    : null;

  return {
    source: 'openalex',
    sourceId: item.id || null,
    isbn: Array.isArray(item.ids?.isbn) ? item.ids.isbn[0] : product.isbn || null,
    title: item.display_name || product.name || null,
    authors,
    publisher: item.host_venue?.publisher || null,
    publishedDate: item.publication_date || null,
    pageCount: Number.isFinite(item.page_count) ? item.page_count : null,
    language: item.language || null,
    categories: Array.isArray(item.keywords) ? item.keywords.slice(0, 8).join('; ') : null,
    averageRating: null,
    ratingsCount: null,
    thumbnailUrl: null,
    infoLink: item.id || null,
    rawPayload: JSON.stringify(item)
  };
}

module.exports = {
  fetchOpenAlex
};
