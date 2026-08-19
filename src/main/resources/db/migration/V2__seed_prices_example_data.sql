-- Datos del enunciado (brand 1 = ZARA, producto 35455). UUIDs fijos: portables entre motores.
insert into prices (id, product_id, brand_id, price_list, valid_from, valid_to, priority, amount, currency) values
    ('0e37eb2c-9a59-4b0e-8b52-61ea86b1c101', 35455, 1, 1, '2020-06-14 00:00:00', '2020-12-31 23:59:59', 0, 35.50, 'EUR'),
    ('0e37eb2c-9a59-4b0e-8b52-61ea86b1c102', 35455, 1, 2, '2020-06-14 15:00:00', '2020-06-14 18:30:00', 1, 25.45, 'EUR'),
    ('0e37eb2c-9a59-4b0e-8b52-61ea86b1c103', 35455, 1, 3, '2020-06-15 00:00:00', '2020-06-15 11:00:00', 1, 30.50, 'EUR'),
    ('0e37eb2c-9a59-4b0e-8b52-61ea86b1c104', 35455, 1, 4, '2020-06-15 16:00:00', '2020-12-31 23:59:59', 1, 38.95, 'EUR');
