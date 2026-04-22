const axios = require('axios');

const GOOGLE_BOOKS_API = 'https://www.googleapis.com/books/v1/volumes';
const REQUEST_TIMEOUT_MS = Number(process.env.REQUEST_TIMEOUT_MS || 20000);

function buildQuery(product) {
  if (product.isbn) {
    return `isbn:${product.isbn}`;
  }
  return `intitle:${product.name}`;
}

async function fetchGoogleBooks(product) {
  const q = buildQuery(product);
  const response = await axios.get(GOOGLE_BOOKS_API, {
    params: {
      q,
      maxResults: 1
    },
    timeout: REQUEST_TIMEOUT_MS
  });

  const data = response.data || {};
  if (!data.items || data.items.length === 0) {
    return null;
  }

  const item = data.items[0];
  const info = item.volumeInfo || {};
  const imageLinks = info.imageLinks || {};

  return {
    source: 'google_books',
    sourceId: item.id || null,
    isbn: (info.industryIdentifiers || []).map((entry) => entry.identifier).find(Boolean) || product.isbn || null,
    title: info.title || product.name || null,
    authors: Array.isArray(info.authors) ? info.authors.join('; ') : null,
    publisher: info.publisher || null,
    publishedDate: info.publishedDate || null,
    pageCount: Number.isFinite(info.pageCount) ? info.pageCount : null,
    language: info.language || null,
    categories: Array.isArray(info.categories) ? info.categories.join('; ') : null,
    averageRating: Number.isFinite(info.averageRating) ? info.averageRating : null,
    ratingsCount: Number.isFinite(info.ratingsCount) ? info.ratingsCount : null,
    thumbnailUrl: imageLinks.thumbnail || imageLinks.smallThumbnail || null,
    infoLink: info.infoLink || null,
    rawPayload: JSON.stringify(item)
  };
}

module.exports = {
  fetchGoogleBooks
};
