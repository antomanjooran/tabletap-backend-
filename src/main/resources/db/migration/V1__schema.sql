-- src/main/resources/db/migration/V1__schema.sql
-- TableTap — Initial Schema
-- Managed by Flyway. Do not edit directly; create V2__... for changes.

-- Extensions
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- ── Tables (restaurant seating) ─────────────────────────────
CREATE TABLE tables (
    id         UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    number     INT         NOT NULL UNIQUE,
    name       VARCHAR(100),
    capacity   INT         NOT NULL DEFAULT 4,
    qr_token   VARCHAR(64) NOT NULL UNIQUE DEFAULT encode(gen_random_bytes(16), 'hex'),
    is_active  BOOLEAN     NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

-- ── Menu ────────────────────────────────────────────────────
CREATE TABLE categories (
    id         UUID         PRIMARY KEY DEFAULT uuid_generate_v4(),
    name       VARCHAR(100) NOT NULL,
    sort_order INT          NOT NULL DEFAULT 0,
    is_active  BOOLEAN      NOT NULL DEFAULT true
);

CREATE TABLE menu_items (
    id           UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    category_id  UUID           NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name         VARCHAR(200)   NOT NULL,
    description  TEXT,
    price        NUMERIC(10, 2) NOT NULL CHECK (price >= 0),
    emoji        VARCHAR(10)    DEFAULT '🍽️',
    image_url    TEXT,
    is_available BOOLEAN        NOT NULL DEFAULT true,
    sort_order   INT            NOT NULL DEFAULT 0,
    created_at   TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at   TIMESTAMPTZ    NOT NULL DEFAULT now()
);

-- ── Orders ──────────────────────────────────────────────────
CREATE TYPE order_status AS ENUM (
    'PENDING', 'CONFIRMED', 'PREPARING', 'READY', 'SERVED', 'CANCELLED'
);

CREATE TYPE payment_method AS ENUM (
    'CARD', 'APPLE_PAY', 'GOOGLE_PAY', 'CASH'
);

CREATE TABLE orders (
    id                        UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    table_id                  UUID           NOT NULL REFERENCES tables(id),
    status                    order_status   NOT NULL DEFAULT 'PENDING',
    notes                     TEXT,
    subtotal                  NUMERIC(10, 2) NOT NULL DEFAULT 0,
    service_charge            NUMERIC(10, 2) NOT NULL DEFAULT 0,
    total                     NUMERIC(10, 2) NOT NULL DEFAULT 0,
    payment_method            payment_method,
    payment_status            VARCHAR(20)    NOT NULL DEFAULT 'UNPAID',
    stripe_payment_intent_id  TEXT,
    created_at                TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at                TIMESTAMPTZ    NOT NULL DEFAULT now()
);

CREATE TABLE order_items (
    id            UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id      UUID           NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    menu_item_id  UUID           NOT NULL REFERENCES menu_items(id),
    quantity      INT            NOT NULL DEFAULT 1 CHECK (quantity > 0),
    unit_price    NUMERIC(10, 2) NOT NULL,
    line_total    NUMERIC(10, 2) GENERATED ALWAYS AS (quantity * unit_price) STORED,
    notes         TEXT,
    created_at    TIMESTAMPTZ    NOT NULL DEFAULT now()
);

-- ── Waiter Calls ────────────────────────────────────────────
CREATE TABLE waiter_calls (
    id          UUID        PRIMARY KEY DEFAULT uuid_generate_v4(),
    table_id    UUID        NOT NULL REFERENCES tables(id),
    message     TEXT        DEFAULT 'Waiter requested',
    is_resolved BOOLEAN     NOT NULL DEFAULT false,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    resolved_at TIMESTAMPTZ
);

-- ── Payments log ────────────────────────────────────────────
CREATE TABLE payments (
    id                    UUID           PRIMARY KEY DEFAULT uuid_generate_v4(),
    order_id              UUID           NOT NULL REFERENCES orders(id),
    stripe_payment_intent TEXT           NOT NULL UNIQUE,
    stripe_charge_id      TEXT,
    amount                NUMERIC(10, 2) NOT NULL,
    currency              VARCHAR(3)     NOT NULL DEFAULT 'gbp',
    status                VARCHAR(20)    NOT NULL,
    created_at            TIMESTAMPTZ    NOT NULL DEFAULT now()
);

-- ── Indexes ─────────────────────────────────────────────────
CREATE INDEX idx_orders_table_id   ON orders(table_id);
CREATE INDEX idx_orders_status     ON orders(status);
CREATE INDEX idx_orders_created_at ON orders(created_at DESC);
CREATE INDEX idx_order_items_order ON order_items(order_id);
CREATE INDEX idx_menu_items_cat    ON menu_items(category_id);
CREATE INDEX idx_waiter_calls_table ON waiter_calls(table_id);
CREATE INDEX idx_tables_qr_token   ON tables(qr_token);

-- ── Trigger: auto-update updated_at ─────────────────────────
CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

CREATE TRIGGER trg_menu_items_updated_at
    BEFORE UPDATE ON menu_items
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();

-- ── Trigger: recalculate order totals ───────────────────────
CREATE OR REPLACE FUNCTION recalculate_order_totals()
RETURNS TRIGGER LANGUAGE plpgsql AS $$
DECLARE
    v_subtotal NUMERIC(10,2);
    v_service  NUMERIC(10,2);
    v_order_id UUID;
BEGIN
    v_order_id := COALESCE(NEW.order_id, OLD.order_id);

    SELECT COALESCE(SUM(line_total), 0)
      INTO v_subtotal
      FROM order_items
     WHERE order_id = v_order_id;

    v_service := ROUND(v_subtotal * 0.125, 2);

    UPDATE orders
       SET subtotal       = v_subtotal,
           service_charge = v_service,
           total          = v_subtotal + v_service
     WHERE id = v_order_id;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_recalc_insert
    AFTER INSERT ON order_items
    FOR EACH ROW EXECUTE FUNCTION recalculate_order_totals();

CREATE TRIGGER trg_recalc_update
    AFTER UPDATE ON order_items
    FOR EACH ROW EXECUTE FUNCTION recalculate_order_totals();

CREATE TRIGGER trg_recalc_delete
    AFTER DELETE ON order_items
    FOR EACH ROW EXECUTE FUNCTION recalculate_order_totals();

-- ── Seed Data ───────────────────────────────────────────────
INSERT INTO tables (number, name, capacity) VALUES
    (1, 'Main Floor 1', 2), (2, 'Main Floor 2', 4),
    (3, 'Main Floor 3', 4), (4, 'Main Floor 4', 6),
    (5, 'Window Seat',  2), (6, 'Bar Table',    2),
    (7, 'Patio 1',      4), (8, 'Patio 2',      4);

INSERT INTO categories (name, sort_order) VALUES
    ('Starters', 1), ('Mains', 2), ('Desserts', 3), ('Drinks', 4);

INSERT INTO menu_items (category_id, name, description, price, emoji, sort_order)
SELECT c.id, m.name, m.description, m.price, m.emoji, m.sort_order
FROM (VALUES
    ('Starters','Burrata & Heritage Tomatoes','Whipped burrata, heirloom tomatoes, basil oil',9.50,'🧀',1),
    ('Starters','Crispy Calamari','Lemon-herb breadcrumb, aioli dipping sauce',8.00,'🦑',2),
    ('Starters','French Onion Soup','Slow-cooked Lyonnaise onion, Gruyère croûte',8.50,'🥣',3),
    ('Starters','Charcuterie Board','Cured meats, pickles, sourdough',14.00,'🍖',4),
    ('Mains','8oz Sirloin Steak','28-day dry-aged, peppercorn sauce, hand-cut chips',28.00,'🥩',1),
    ('Mains','Pan-Seared Salmon','Lemon butter, asparagus, new potatoes',22.00,'🐟',2),
    ('Mains','Wild Mushroom Risotto','Arborio rice, truffle oil, Parmesan',18.00,'🍄',3),
    ('Mains','Roast Chicken Supreme','Free-range, tarragon jus, root vegetables',20.00,'🍗',4),
    ('Mains','Truffle Tagliatelle','Fresh pasta, black truffle, cream, Parmigiano',19.00,'🍝',5),
    ('Desserts','Crème Brûlée','Classic vanilla, caramelised sugar crust',7.50,'🍮',1),
    ('Desserts','Chocolate Fondant','Warm dark chocolate, vanilla ice cream',8.00,'🍫',2),
    ('Desserts','Seasonal Fruit Sorbet','Three scoops, fresh mint garnish',6.50,'🍨',3),
    ('Drinks','House Red Wine (175ml)','Malbec, Mendoza — bold & fruity',7.50,'🍷',1),
    ('Drinks','Craft IPA','Local brewery, cold filtered, hoppy finish',6.00,'🍺',2),
    ('Drinks','Still / Sparkling Water','750ml carafe',3.50,'💧',3),
    ('Drinks','Freshly Squeezed Juice','Orange, apple, or grapefruit',4.50,'🍊',4)
) AS m(cat_name, name, description, price, emoji, sort_order)
JOIN categories c ON c.name = m.cat_name;
