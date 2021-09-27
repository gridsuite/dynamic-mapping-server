-- Temporary models values while no user interface for them is provided.

INSERT INTO models (model_name, equipment_type)
VALUES ('GeneratorThreeWindings',
        0),
       ('GeneratorFourWindings',
        0),
       ('LoadAlphaBeta',
        1),
       ('CLA_2_4', 2);

INSERT INTO model_sets_group (model_name, name, type)
VALUES ('GeneratorThreeWindings', 'GSTWPR', 1),
       ('GeneratorFourWindings', 'GSFWPR', 1),
       ('LoadAlphaBeta', 'LAB', 0),
       ('CLA_2_4', 'CLA_2_4', 0);

INSERT INTO model_parameter_definitions (model_name, name, origin, origin_name, type)
VALUES ('LoadAlphaBeta',
        'load_alpha', 2,
        NULL, 2),
       ('LoadAlphaBeta',
        'load_beta', 2,
        NULL, 2),
       ('LoadAlphaBeta',
        'load_P0Pu', 0,
        'p_pu', 2),
       ('LoadAlphaBeta',
        'load_Q0Pu', 0,
        'q_pu', 2),
       ('LoadAlphaBeta',
        'load_U0Pu', 0,
        'v_pu', 2),
       ('LoadAlphaBeta',
        'load_UPhase0', 0,
        'angle_pu', 2);