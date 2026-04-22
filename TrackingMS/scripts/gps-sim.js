const ORDER_ID = 13;
const COURIER_ID = 6;
const TOKEN = 'Bearer eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjoiQURNSU4iLCJ1c2VySWQiOjYsInN0YXR1cyI6IkFDVElWRSIsInVzZXJuYW1lIjoiYWRtaW5AZXNwcml0LnRuIiwic3ViIjoiYWRtaW5AZXNwcml0LnRuIiwiaWF0IjoxNzc2MzE0Njc4LCJleHAiOjE3NzY0MDEwNzh9.XLm2E-YS01dYyCxuqUTtVlFJhZN5m7Wkll9xc9rO6FQ';
                        
const PICKUP = [36.899809, 10.187689];
const DELIVERY = [36.778916, 10.227846];
const FALLBACK_POINTS = [
  [36.899809, 10.187689],
  [36.8056, 10.1846],
  [36.8048, 10.1878],
  [36.8036, 10.1914],
  [36.8019, 10.1955],
  [36.7998, 10.1994],
  [36.7977, 10.2032],
  [36.7957, 10.2068],
  [36.7938, 10.2102],
  [36.7921, 10.2136],
  [36.7905, 10.2172],
  [36.7892, 10.2209],
  [36.7881, 10.2247],
  [36.7874, 10.2286],
  [36.7870, 10.2326],
  [36.7869, 10.2366]
];

let routePoints = FALLBACK_POINTS;

let index = 0;

async function loadRoutePoints() {
  const [pickupLat, pickupLng] = PICKUP;
  const [deliveryLat, deliveryLng] = DELIVERY;
  const url = `http://router.project-osrm.org/route/v1/driving/${pickupLng},${pickupLat};${deliveryLng},${deliveryLat}?overview=full&geometries=geojson`;

  try {
    const res = await fetch(url);
    if (!res.ok) {
      console.warn(`OSRM route failed: ${res.status} ${res.statusText}`);
      return;
    }
    const data = await res.json();
    const coords = data?.routes?.[0]?.geometry?.coordinates;
    if (!Array.isArray(coords) || coords.length === 0) {
      console.warn('OSRM route returned no coordinates.');
      return;
    }
    const points = coords.map(([lng, lat]) => [lat, lng]);
    routePoints = reducePoints(points, 6);
  } catch (error) {
    console.warn('OSRM route error, using fallback points.', error?.message || error);
  }
}

function reducePoints(points, step) {
  if (!points.length || step <= 1) {
    return points;
  }
  const reduced = [];
  for (let i = 0; i < points.length; i += step) {
    reduced.push(points[i]);
  }
  if (reduced[reduced.length - 1] !== points[points.length - 1]) {
    reduced.push(points[points.length - 1]);
  }
  return reduced;
}

async function sendPoint() {
  const [lat, lng] = routePoints[index];
  index = (index + 1) % routePoints.length;

  const body = {
    orderId: ORDER_ID,
    courierId: COURIER_ID,
    lat,
    lng,
    speed: 12,
    heading: 90,
    timestamp: new Date().toISOString()
  };

  try {
    const res = await fetch('http://localhost:8087/tracking/update', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        Authorization: TOKEN
      },
      body: JSON.stringify(body)
    });

    if (!res.ok) {
      const text = await res.text();
      console.error(`Update failed: ${res.status} ${res.statusText} - ${text}`);
      return;
    }

    console.log(`Sent: ${lat}, ${lng}`);
  } catch (error) {
    console.error('Request error:', error.message || error);
  }
}

async function start() {
  await loadRoutePoints();
  setInterval(sendPoint, 3000);
}

start();
