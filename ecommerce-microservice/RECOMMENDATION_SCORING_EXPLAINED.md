# Recommendation Scoring (Category + Rating + Recency)

Date: 2026-03-28
Scope: Ecommerce recommendations only

## Purpose
Improve the "related", "bestsellers", and "trending" sections by ranking products with a transparent score that blends:
- Category match
- Customer rating
- Recency

This produces recommendations that are both relevant and academically explainable.

---

## What the score means
The score is a numeric value between 0 and 1 (or 0 and 100) that measures how suitable a product is for recommendation.
Higher score = higher priority in the recommendation list.

A simple formula:

score = (wCategory * categoryMatch)
      + (wRating * ratingScore)
      + (wRecency * recencyScore)

Where:
- categoryMatch is either 1 (same category) or 0 (different category).
- ratingScore is a normalized rating in range 0..1 (e.g., averageRating / 5).
- recencyScore is a normalized value based on how new the product is.

---

## Recency explained (simple)
Recency is a way to favor newer products. We compute it from time since product creation.

Example rule:
- If product is 0 days old -> recencyScore = 1.0
- If product is 30 days old -> recencyScore = 0.0
- If product is 15 days old -> recencyScore = 0.5

So, recencyScore = max(0, 1 - (ageInDays / 30))

This means newer products get a boost without ignoring older products completely.

---

## Weights explained
Weights decide how much each factor matters. They must sum to 1.

Example weights (balanced):
- wCategory = 0.45
- wRating = 0.35
- wRecency = 0.20

Interpretation:
- Category relevance matters most.
- Rating matters a lot but is not everything.
- Recency helps surface new items but does not dominate.

If you prefer another balance, we can change these values.

---

## How it will work in the code
1) Load candidate products.
2) For each product, compute:
   - categoryMatch (1 or 0)
   - ratingScore (averageRating / 5)
   - recencyScore based on createdAt
3) Compute final score using weights.
4) Sort by score descending.
5) Return top N products.

---

## Where we will apply it
- Recommendations endpoint in ecommerce service:
  - Related products for a product
  - Bestsellers
  - Trending

We will adjust existing methods to call a scoring helper and sort results.

---

## Config (optional)
If you want these weights configurable later, we can store them in application.properties.
If not, we hardcode them in the service for simplicity.

---

## Example for a product
Assume:
- Same category -> categoryMatch = 1
- averageRating = 4.2 -> ratingScore = 0.84
- Age = 10 days -> recencyScore = 1 - (10/30) = 0.67

Using weights 0.45 / 0.35 / 0.20:
score = (0.45 * 1) + (0.35 * 0.84) + (0.20 * 0.67)
score = 0.45 + 0.294 + 0.134
score = 0.878

Higher score = higher ranking in recommendations.

---

## Next step
Weights and recency window were finalized as:
- Recency window: 30 days
- Weights: 0.45 category, 0.35 rating, 0.20 recency

Implementation is now in ProductServiceImpl with scoring and re-ranking applied to related, bestsellers, and trending lists.
