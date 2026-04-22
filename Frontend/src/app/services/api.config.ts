// All APIs go through API Gateway
export const API_BASE_URL = 'http://localhost:8080/api';

// Courses are called directly for faster local CRUD feedback during development.
export const COURSE_API_BASE_URL = 'http://localhost:8081/api';

// AI assistant endpoint (Python bridge for Ollama/Mistral)
export const COURSE_ASSISTANT_API_URL = 'http://localhost:8001/chat';

// Training Service (routed through gateway)
export const TRAINING_API_BASE_URL = API_BASE_URL;

// Chat Service (Microservice Port 8083)
export const CHAT_API_URL = 'http://localhost:8083/api/chat';
export const CHAT_WS_URL = 'http://localhost:8083/api/ws';

// Game Service (Microservice Port 8084)
export const GAME_API_URL = 'http://localhost:8084/api';
