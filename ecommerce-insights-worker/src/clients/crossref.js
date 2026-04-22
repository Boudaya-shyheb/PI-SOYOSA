const axios = require('axios');

const CROSSREF_API = 'https://api.crossref.org/works';
const REQUEST_TIMEOUT_MS = Number(process.env.REQUEST_TIMEOUT_MS || 20000);

function buildParams(product) {
  if (product.isbn) {
    return { filter: `isbn:${product.isbn}`, rows: 1 };
  }
  return { query: product.name, rows: 1 };
}

async function fetchCrossref(product) {
  const params = buildParams(product);
  const response = await axios.get(CROSSREF_API, {
    params,
    timeout: REQUEST_TIMEOUT_MS,
    headers: {
      'User-Agent': 'ecommerce-insights-worker/1.0 (mailto:admin@example.com)'
    }
  });

  const data = response.data?.message || {};
  const items = data.items || [];
  if (items.length === 0) {
    return null;
  }

  const item = items[0];
  const authorNames = Array.isArray(item.author)
    ? item.author
      .map((author) => [author.given, author.family].filter(Boolean).join(' '))
      .filter(Boolean)
      .join('; ')
    : null;

  return {
    source: 'crossref',
    sourceId: item.DOI || null,
    isbn: Array.isArray(item.ISBN) ? item.ISBN[0] : product.isbn || null,
    title: Array.isArray(item.title) ? item.title[0] : product.name || null,
    authors: authorNames,
    publisher: item.publisher || null,
    publishedDate: item.created?.['date-time'] || null,
    pageCount: null,
    language: item.language || null,
    categories: Array.isArray(item.subject) ? item.subject.slice(0, 8).join('; ') : null,
    averageRating: null,
    ratingsCount: null,
    thumbnailUrl: null,
    infoLink: item.URL || null,
    rawPayload: JSON.stringify(item)
  };
}

module.exports = {
  fetchCrossref
};
