-- Temporary models values while no user interface for them is provided.

INSERT INTO instance_models (id, modelname, equipmenttype, params_id, params_type)
    VALUES (
        'GeneratorSynchronousThreeWindingsProportionalRegulations',
        'GeneratorThreeWindings',
        0,
        'GSTWPR',
        1
    ), (
        'GeneratorSynchronousFourWindingsProportionalRegulations',
        'GeneratorFourWindings',
        0,
        'GSFWPR',
        1
    ),(
        'LoadLab',
        'LoadAlphaBeta',
        1,
        'LAB',
        0
);