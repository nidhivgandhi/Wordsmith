INSERT INTO story_structures (name, slug, description)
VALUES ('Save the Cat', 'save-the-cat',
        'Blake Snyder''s 15-beat screenwriting structure, widely adapted for novels.');

INSERT INTO structure_beats (structure_id, name, description, position)
SELECT s.id, b.name, b.description, b.position
FROM story_structures s,
(VALUES
   ('Opening Image',          'A snapshot of the hero''s world before the story begins; sets tone and stakes.',        1),
   ('Theme Stated',           'Someone states the lesson the hero must learn by the end.',                             2),
   ('Set-Up',                 'Establish the hero, their world, and what is missing in their life.',                   3),
   ('Catalyst',               'The inciting incident that knocks the hero out of their status quo.',                   4),
   ('Debate',                 'The hero hesitates: should they answer the call? Raises the central question.',         5),
   ('Break into Two',         'The hero makes a choice and steps into a new world (Act 2).',                           6),
   ('B Story',                'A secondary story or relationship begins, often carrying the theme.',                   7),
   ('Fun and Games',          'The "promise of the premise" - the hero explores the new world; set-pieces land here.', 8),
   ('Midpoint',               'A false victory or false defeat that raises the stakes and turns the story.',           9),
   ('Bad Guys Close In',      'Internal and external pressure mounts; the hero''s team frays.',                        10),
   ('All Is Lost',            'The hero''s lowest point; often a "whiff of death".',                                   11),
   ('Dark Night of the Soul', 'The hero wallows in defeat before finding a new insight.',                             12),
   ('Break into Three',       'Armed with the theme''s lesson, the hero finds the solution (Act 3).',                  13),
   ('Finale',                 'The hero executes the plan, proves they have changed, and resolves the conflict.',      14),
   ('Final Image',            'A closing snapshot that mirrors the opening image and shows how much has changed.',     15)
) AS b(name, description, position)
WHERE s.slug = 'save-the-cat';
