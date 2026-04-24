-- ============================================================
-- STEP 1: Run this SQL in your Supabase SQL Editor
-- Dashboard → SQL Editor → New Query → Paste & Run
-- ============================================================

CREATE TABLE IF NOT EXISTS public.ingredients (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        TEXT NOT NULL,
    category    TEXT NOT NULL,
    quantity    NUMERIC(10, 2) NOT NULL DEFAULT 0,
    unit        TEXT NOT NULL DEFAULT 'pcs',
    price       NUMERIC(10, 2) NOT NULL DEFAULT 0,
    expiry_date DATE NOT NULL,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ============================================================
-- STEP 2: Enable Row Level Security (RLS) — recommended
-- ============================================================

ALTER TABLE public.ingredients ENABLE ROW LEVEL SECURITY;

-- Allow anonymous (public) read access
CREATE POLICY "Allow public read"
    ON public.ingredients
    FOR SELECT
    USING (true);

-- Allow anonymous insert
CREATE POLICY "Allow public insert"
    ON public.ingredients
    FOR INSERT
    WITH CHECK (true);

-- Allow anonymous update
CREATE POLICY "Allow public update"
    ON public.ingredients
    FOR UPDATE
    USING (true);

-- Allow anonymous delete
CREATE POLICY "Allow public delete"
    ON public.ingredients
    FOR DELETE
    USING (true);

-- ============================================================
-- STEP 3 (optional): Add some sample data to test
-- ============================================================

INSERT INTO public.ingredients (name, category, quantity, unit, price, expiry_date) VALUES
    ('Chicken Breast',  'Protein',   1.5,  'kg',  12.90, CURRENT_DATE + INTERVAL '3 days'),
    ('Whole Milk',      'Dairy',     2.0,  'L',    6.50, CURRENT_DATE + INTERVAL '5 days'),
    ('Broccoli',        'Vegetable', 500,  'g',    3.20, CURRENT_DATE + INTERVAL '7 days'),
    ('Brown Rice',      'Grain',     1.0,  'kg',   8.00, CURRENT_DATE + INTERVAL '180 days'),
    ('Orange',          'Fruit',     6.0,  'pcs',  5.00, CURRENT_DATE + INTERVAL '10 days'),
    ('Vitamin C',       'Vitamin',   30.0, 'tabs',18.00, CURRENT_DATE + INTERVAL '365 days'),
    ('Cumin Powder',    'Spice',     50.0, 'g',    2.50, CURRENT_DATE + INTERVAL '730 days'),
    ('Expired Egg',     'Protein',   6.0,  'pcs',  4.50, CURRENT_DATE - INTERVAL '2 days');
