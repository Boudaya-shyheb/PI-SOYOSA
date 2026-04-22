const axios = require('axios');

const OPEN_LIBRARY_SEARCH = 'https://openlibrary.org/search.json';
const REQUEST_TIMEOUT_MS = Number(process.env.REQUEST_TIMEOUT_MS || 20000);

function buildQuery(product) {
  if (product.isbn) {
    return { isbn: product.isbn };
  }
  return { title: product.name };
}

async function fetchOpenLibrary(product) {
  const query = buildQuery(product);
  const response = await axios.get(OPEN_LIBRARY_SEARCH, {
    params: {
      ...query,
      limit: 1
    },
    timeout: REQUEST_TIMEOUT_MS
  });

  const data = response.data || {};
  if (!data.docs || data.docs.length === 0) {
    return null;
  }

  const doc = data.docs[0];
  const isbn = Array.isArray(doc.isbn) ? doc.isbn.find(Boolean) : product.isbn;

  return {
    source: 'open_library',
    sourceId: doc.key || null,
    isbn: isbn || product.isbn || null,
    title: doc.title || product.name || null,
    authors: Array.isArray(doc.author_name) ? doc.author_name.join('; ') : null,
    publisher: Array.isArray(doc.publisher) ? doc.publisher[0] : null,
    publishedDate: doc.first_publish_year ? String(doc.first_publish_year) : null,
    pageCount: Number.isFinite(doc.number_of_pages_median) ? doc.number_of_pages_median : null,
    language: Array.isArray(doc.language) ? doc.language.join('; ') : null,
    categories: Array.isArray(doc.subject) ? doc.subject.slice(0, 8).join('; ') : null,
    averageRating: null,
    ratingsCount: null,
    thumbnailUrl: doc.cover_i ? `https://covers.openlibrary.org/b/id/${doc.cover_i}-L.jpg` : null,
    infoLink: doc.key ? `https://openlibrary.org${doc.key}` : null,
    rawPayload: JSON.stringify(doc)
  };
}

module.exports = {
  fetchOpenLibrary
};
