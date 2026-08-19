create table prices (
    id         uuid          primary key,
    product_id bigint        not null,
    brand_id   bigint        not null,
    price_list integer       not null,
    valid_from timestamp     not null,
    valid_to   timestamp     not null,
    priority   integer       not null,
    amount     numeric(10,2) not null,
    currency   varchar(3)    not null
);

-- Todas las lecturas buscan candidatas por producto y cadena vigentes en una fecha.
create index idx_prices_lookup on prices (product_id, brand_id, valid_from, valid_to);
