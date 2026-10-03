-- Imagen opcional (Cloudinary) de productos y servicios.
ALTER TABLE productos ADD COLUMN imagen_url VARCHAR(500), ADD COLUMN imagen_public_id VARCHAR(255);
ALTER TABLE servicios ADD COLUMN imagen_url VARCHAR(500), ADD COLUMN imagen_public_id VARCHAR(255);
