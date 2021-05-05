},{
      "id": "GeneratorThreeWindings",
      "type": "GENERATOR",
      "name": "GeneratorSynchronousThreeWindingsProportionalRegulations",
      "params": {
        "type": "PREFIX",
        "name": "GSTWPR"
      }
    }

INSERT INTO instance_models (id, modelname, equipmenttype, params_id, params_type)
    VALUES (
        'GeneratorSynchronousThreeWindingsProportionalRegulations',
        'GeneratorThreeWindings',
        'GENERATOR',
        'GSTWPR',
        'PREFIX'
    ), (
        'GeneratorSynchronousFourWindingsProportionalRegulations',
        'GeneratorFourWindings',
        'GENERATOR',
        'GSFWPR',
        'PREFIX'
    ),(
        'LoadAlphaBeta',
        'LoadLab',
        'LOAD',
        'LAB',
        'FIXED'
);