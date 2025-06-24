CREATE TABLE users
(
    id         BIGSERIAL PRIMARY KEY,
    login_id   VARCHAR(255) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    nickname   VARCHAR(255) NOT NULL,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP    NULL
);

CREATE TABLE parties
(
    id          BIGSERIAL PRIMARY KEY,
    host_id     BIGINT       NOT NULL,
    name        VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    date        TIMESTAMP,
    location    VARCHAR(255),
    status      VARCHAR(20)  NOT NULL,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at  TIMESTAMP
);
CREATE INDEX idx_host_id_status ON parties (host_id, status);

CREATE TABLE invite_codes
(
    id         BIGSERIAL PRIMARY KEY,
    party_id   BIGINT       NOT NULL,
    code       VARCHAR(100) NOT NULL UNIQUE,
    is_active  BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_party_id_code ON invite_codes (party_id, code);
CREATE UNIQUE INDEX unique_active_invite_per_party ON invite_codes (party_id) WHERE is_active = true;

CREATE TABLE party_guests
(
    id         BIGSERIAL PRIMARY KEY,
    party_id   BIGINT       NOT NULL,
    nickname   VARCHAR(100) NOT NULL,
    is_blocked BOOLEAN      NOT NULL DEFAULT FALSE,
    joined_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP,
    UNIQUE (party_id, nickname)
);

CREATE TABLE categories
(
    id            BIGSERIAL PRIMARY KEY,
    party_id      BIGINT       NOT NULL,
    name          VARCHAR(100) NOT NULL,
    display_order INTEGER      NOT NULL DEFAULT 0,
    created_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at    TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (party_id, name)
);

CREATE TABLE menus
(
    id             BIGSERIAL PRIMARY KEY,
    category_id    BIGINT       NOT NULL,
    name           VARCHAR(100) NOT NULL,
    description    TEXT,
    is_recommended BOOLEAN      NOT NULL DEFAULT FALSE,
    is_sold_out    BOOLEAN      NOT NULL DEFAULT FALSE,
    display_order  INTEGER      NOT NULL DEFAULT 0,
    created_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (category_id, name)
);

CREATE TABLE option_groups
(
    id          BIGSERIAL PRIMARY KEY,
    menu_id     BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    is_required BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (menu_id, name)
);

CREATE TABLE options
(
    id              BIGSERIAL PRIMARY KEY,
    option_group_id BIGINT       NOT NULL,
    name            VARCHAR(100) NOT NULL,
    display_order   INTEGER      NOT NULL DEFAULT 0,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (option_group_id, name)
);

CREATE TABLE orders
(
    id         BIGSERIAL PRIMARY KEY,
    party_id   BIGINT      NOT NULL,
    guest_id   BIGINT      NOT NULL,
    status     VARCHAR(20) NOT NULL,
    ordered_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_party_id_guest_id_status ON orders (party_id, guest_id, status);
CREATE INDEX idx_orders_status ON orders (status);

CREATE TABLE order_items
(
    id         BIGSERIAL PRIMARY KEY,
    order_id   BIGINT    NOT NULL,
    menu_id    BIGINT    NOT NULL,
    quantity   INTEGER   NOT NULL DEFAULT 1,
    notes      TEXT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (order_id, menu_id)
);

CREATE TABLE order_item_options
(
    id            BIGSERIAL PRIMARY KEY,
    order_item_id BIGINT    NOT NULL,
    option_id     BIGINT    NOT NULL,
    created_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (order_item_id, option_id)
);