INSERT INTO sectors (id, name) VALUES
        (gen_random_uuid(), 'Agriculture'),
        (gen_random_uuid(), 'Bakery'),
        (gen_random_uuid(), 'Construction'),
        (gen_random_uuid(), 'Consulting'),
        (gen_random_uuid(), 'Education'),
        (gen_random_uuid(), 'Food & Beverage'),
        (gen_random_uuid(), 'Healthcare'),
        (gen_random_uuid(), 'Retail'),
        (gen_random_uuid(), 'Restaurant'),
        (gen_random_uuid(), 'Technology'),
        (gen_random_uuid(), 'Transport'),
        (gen_random_uuid(), 'Wholesale'),
        (gen_random_uuid(), 'Other')
ON CONFLICT (name) DO NOTHING;