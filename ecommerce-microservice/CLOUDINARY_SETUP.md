# Cloudinary Image Upload Setup

Date: 2026-03-28
Scope: Shop admin product/bundle images

## Required Cloudinary values
- Cloud name: dbocjw6wd
- Upload preset: ml_default (unsigned)

## Cloudinary console steps
1) Cloudinary Dashboard -> Settings -> Upload.
2) Upload presets -> Add upload preset.
3) Set unsigned: yes.
4) Folder: ecommerce
5) Allowed formats: jpg, png, webp
6) Save preset and copy its name.

## Frontend configuration
Set these in Angular environments:
- cloudinaryCloudName: dbocjw6wd
- cloudinaryUploadPreset: ml_default

Files:
- Frontend/src/app/environments/environment.ts
- Frontend/src/app/environments/environment.prod.ts

## How uploads work
- Admin selects a local image file.
- Frontend uploads the file to Cloudinary using the unsigned preset.
- Cloudinary returns a public URL.
- That URL is saved into product/bundle imageUrl.

## Frontend test steps
1) Login as admin.
2) Go to /shop/admin.
3) Choose a local image file for a product.
4) Wait for "Image uploaded successfully" toast.
5) Save the product. The image should appear in catalog and admin list.

## Notes
- Local files are no longer stored as base64 in DB.
- If upload fails, check the preset and CORS in Cloudinary settings.
