INSERT INTO story_structures (name, slug, description)
VALUES ('Three-Act Structure', 'three-act',
        'The classic dramatic structure: Setup, Confrontation, Resolution.');

INSERT INTO structure_beats (structure_id, name, description, position)
SELECT s.id, b.name, b.description, b.position
FROM story_structures s,
(VALUES
   ('Setup',             'Establish characters, world, and the status quo.', 1),
   ('Inciting Incident', 'The event that disrupts the status quo.',           2),
   ('Plot Point One',    'Protagonist commits to the conflict; ends Act 1.',  3),
   ('Midpoint',          'A major turn that raises the stakes.',              4),
   ('Plot Point Two',    'The lowest point; ends Act 2.',                     5),
   ('Climax',            'The final confrontation.',                          6),
   ('Resolution',        'The new status quo; loose ends tied up.',           7)
) AS b(name, description, position)
WHERE s.slug = 'three-act';