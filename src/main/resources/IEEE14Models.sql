-- Temporary models values while no user interface for them is provided.

INSERT INTO models (model_name, equipment_type, created_date)
VALUES ('GeneratorSynchronousThreeWindings', 0, now()::timestamp),
       ('GeneratorSynchronousFourWindings', 0, now()::timestamp),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 0, now()::timestamp),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 0, now()::timestamp),
       ('GeneratorPQ', 0, now()::timestamp),
       ('GeneratorPV', 0, now()::timestamp),
       ('LoadAlphaBeta', 1, now()::timestamp),
       ('LoadPQ', 1, now()::timestamp),
       ('CurrentLimitAutomaton', 2, now()::timestamp);

INSERT INTO model_sets_group (model_name, name, type)
VALUES ('GeneratorSynchronousThreeWindings', 'GSTW', 0),
       ('GeneratorSynchronousFourWindings', 'GSFW', 0),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'GSTWPR', 0),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'GSFWPR', 0),
       ('GeneratorPQ', 'GPQ', 0),
       ('GeneratorPV', 'GPV', 0),
       ('LoadAlphaBeta', 'LAB', 0),
       ('LoadPQ', 'LPQ', 0),
       ('CurrentLimitAutomaton', 'CLA', 0),
       ('CurrentLimitAutomaton', 'CLA_2_4', 0),
       ('CurrentLimitAutomaton', 'CLA_2_5', 0);

INSERT INTO model_parameter_sets (group_name, group_type, model_name, name)
VALUES ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'GSTW'),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'GSFW'),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'GSTWPR'),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'GSFWPR'),
       ('GPQ', 0, 'GeneratorPQ', 'GPQ'),
       ('GPV', 0, 'GeneratorPV', 'GPV'),
       ('LAB', 0, 'LoadAlphaBeta', 'LAB'),
       ('LPQ', 0, 'LoadPQ', 'LPQ'),
       ('CLA', 0, 'CurrentLimitAutomaton', 'CLA'),
       ('CLA_2_4', 0, 'CurrentLimitAutomaton', 'CLA_2_4'),
       ('CLA_2_5', 0, 'CurrentLimitAutomaton', 'CLA_2_5');

INSERT INTO model_parameter_definitions (name, type, fixed_value, created_date)
VALUES  ('generator_UNom', 2, NULL, now()::timestamp),
        ('generator_SNom', 2, NULL, now()::timestamp),
        ('generator_PNomTurb', 2, NULL, now()::timestamp),
        ('generator_PNomAlt', 2, NULL, now()::timestamp),
        ('generator_ExcitationPu', 0, 1, now()::timestamp),
        ('generator_MdPuEfd', 2, NULL, now()::timestamp),
        ('generator_H', 2, NULL, now()::timestamp),
        ('generator_DPu', 2, NULL, now()::timestamp),
        ('generator_SnTfo', 2, NULL, now()::timestamp),
        ('generator_UNomHV', 2, NULL, now()::timestamp),
        ('generator_UNomLV', 2, NULL, now()::timestamp),
        ('generator_UBaseHV', 2, NULL, now()::timestamp),
        ('generator_UBaseLV', 2, NULL, now()::timestamp),
        ('generator_RTfPu', 2, NULL, now()::timestamp),
        ('generator_XTfPu', 2, NULL, now()::timestamp),
        ('generator_RaPu', 2, NULL, now()::timestamp),
        ('generator_XlPu', 2, NULL, now()::timestamp),
        ('generator_XdPu', 2, NULL, now()::timestamp),
        ('generator_XpdPu', 2, NULL, now()::timestamp),
        ('generator_XppdPu', 2, NULL, now()::timestamp),
        ('generator_Tpd0', 2, NULL, now()::timestamp),
        ('generator_Tppd0', 2, NULL, now()::timestamp),
        ('generator_XqPu', 2, NULL, now()::timestamp),
        ('generator_XppqPu', 2, NULL, now()::timestamp),
        ('generator_Tppq0', 2, NULL, now()::timestamp),
        ('generator_md', 2, NULL, now()::timestamp),
        ('generator_mq', 2, NULL, now()::timestamp),
        ('generator_nd', 2, NULL, now()::timestamp),
        ('generator_nq', 2, NULL, now()::timestamp),
        ('URef_ValueIn', 2, 0, now()::timestamp),
        ('Pm_ValueIn', 2, 0, now()::timestamp),
        ('generator_P0Pu', 2, NULL, now()::timestamp),
        ('generator_Q0Pu', 2, NULL, now()::timestamp),
        ('generator_UPhase0', 2, NULL, now()::timestamp),
        ('generator_UseApproximation', 1, NULL, now()::timestamp),
        ('generator_U0Pu', 2, NULL, now()::timestamp),
        ('generator_XpqPu', 2, NULL, now()::timestamp),
        ('generator_Tpq0', 2, NULL, now()::timestamp),
        ('governor_KGover', 2, NULL, now()::timestamp),
        ('governor_PMin', 2, NULL, now()::timestamp),
        ('governor_PMax', 2, NULL, now()::timestamp),
        ('governor_PNom', 2, NULL, now()::timestamp),
        ('voltageRegulator_LagEfdMin', 2, NULL, now()::timestamp),
        ('voltageRegulator_LagEfdMax', 2, NULL, now()::timestamp),
        ('voltageRegulator_EfdMinPu', 2, NULL, now()::timestamp),
        ('voltageRegulator_EfdMaxPu', 2, NULL, now()::timestamp),
        ('voltageRegulator_UsRefMinPu', 2, NULL, now()::timestamp),
        ('voltageRegulator_UsRefMaxPu', 2, NULL, now()::timestamp),
        ('voltageRegulator_Gain', 2, NULL, now()::timestamp),
        ('generator_AlphaPuPNom', 2, NULL, now()::timestamp),
        ('generator_PNom', 2, NULL, now()::timestamp),
        ('generator_PMin', 2, NULL, now()::timestamp),
        ('generator_PMax', 2, NULL, now()::timestamp),
        ('generator_LambdaPuSNom', 2, NULL, now()::timestamp),
        ('generator_QMax', 2, NULL, now()::timestamp),
        ('generator_QMin', 2, NULL, now()::timestamp);

INSERT INTO models_model_parameter_definitions (model_name, parameter_definition_name, origin, origin_name)
VALUES ('GeneratorSynchronousThreeWindings', 'generator_UNom', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_SNom', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_PNomTurb', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_PNomAlt', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_ExcitationPu', 1, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_MdPuEfd', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_H', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_DPu', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_SnTfo', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_UNomHV', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_UNomLV', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_UBaseHV', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_UBaseLV', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_RTfPu', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_XTfPu', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_RaPu', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_XlPu', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_XdPu', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_XpdPu', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_XppdPu', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_Tpd0', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_Tppd0', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_XqPu', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_XppqPu', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_Tppq0', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_md', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_mq', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_nd', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_nq', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'URef_ValueIn', 1, NULL),
       ('GeneratorSynchronousThreeWindings', 'Pm_ValueIn', 1, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_P0Pu', 0, 'p_pu'),
       ('GeneratorSynchronousThreeWindings', 'generator_Q0Pu', 0, 'q_pu'),
       ('GeneratorSynchronousThreeWindings', 'generator_UPhase0', 0, 'angle_pu'),
       ('GeneratorSynchronousThreeWindings', 'generator_UseApproximation', 2, NULL),
       ('GeneratorSynchronousThreeWindings', 'generator_U0Pu', 0, 'v_pu');

INSERT INTO models_model_parameter_definitions (model_name, parameter_definition_name, origin, origin_name)
VALUES ('GeneratorSynchronousFourWindings', 'generator_UNom', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_SNom', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_PNomTurb', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_PNomAlt', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_ExcitationPu', 1, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_MdPuEfd', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_H', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_DPu', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_SnTfo', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_UNomHV', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_UNomLV', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_UBaseHV', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_UBaseLV', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_RTfPu', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_XTfPu', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_RaPu', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_XlPu', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_XdPu', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_XpdPu', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_XppdPu', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_Tpd0', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_Tppd0', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_XqPu', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_XppqPu', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_Tppq0', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_md', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_mq', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_nd', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_nq', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_XpqPu', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_Tpq0', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'URef_ValueIn', 1, NULL),
       ('GeneratorSynchronousFourWindings', 'Pm_ValueIn', 1, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_P0Pu', 0, 'p_pu'),
       ('GeneratorSynchronousFourWindings', 'generator_Q0Pu', 0, 'q_pu'),
       ('GeneratorSynchronousFourWindings', 'generator_UPhase0', 0, 'angle_pu'),
       ('GeneratorSynchronousFourWindings', 'generator_UseApproximation', 2, NULL),
       ('GeneratorSynchronousFourWindings', 'generator_U0Pu', 0, 'v_pu');

INSERT INTO models_model_parameter_definitions (model_name, parameter_definition_name, origin, origin_name)
VALUES ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UNom', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_SNom', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_PNomTurb', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_PNomAlt', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_ExcitationPu', 1, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_MdPuEfd', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_H', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_DPu', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_SnTfo', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UNomHV', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UNomLV', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UBaseHV', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UBaseLV', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_RTfPu', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XTfPu', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_RaPu', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XlPu', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XdPu', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XpdPu', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XppdPu', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Tpd0', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Tppd0', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XqPu', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XppqPu', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Tppq0', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_md', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_mq', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_nd', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_nq', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_KGover', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_PMin', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_PMax', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_PNom', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_LagEfdMin', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_LagEfdMax', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_EfdMinPu', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_EfdMaxPu', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_UsRefMinPu', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_UsRefMaxPu', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_Gain', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'URef_ValueIn', 1, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'Pm_ValueIn', 1, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_P0Pu', 0, 'p_pu'),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Q0Pu', 0, 'q_pu'),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UPhase0', 0, 'angle_pu'),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UseApproximation', 2, NULL),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_U0Pu', 0, 'v_pu');

INSERT INTO models_model_parameter_definitions (model_name, parameter_definition_name, origin, origin_name)
VALUES ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UNom', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_SNom', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_PNomTurb', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_PNomAlt', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_ExcitationPu', 1, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_MdPuEfd', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_H', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_DPu', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_SnTfo', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UNomHV', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UNomLV', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UBaseHV', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UBaseLV', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_RTfPu', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XTfPu', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_RaPu', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XlPu', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XdPu', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XpdPu', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XppdPu', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tpd0', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tppd0', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XqPu', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XppqPu', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tppq0', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_md', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_mq', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_nd', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_nq', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XpqPu', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tpq0', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_KGover', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_PMin', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_PMax', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_PNom', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_LagEfdMin', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_LagEfdMax', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_EfdMinPu', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_EfdMaxPu', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_UsRefMinPu', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_UsRefMaxPu', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_Gain', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'URef_ValueIn', 1, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'Pm_ValueIn', 1, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_P0Pu', 0, 'p_pu'),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Q0Pu', 0, 'q_pu'),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UPhase0', 0, 'angle_pu'),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UseApproximation', 2, NULL),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_U0Pu', 0, 'v_pu');

INSERT INTO models_model_parameter_definitions (model_name, parameter_definition_name, origin, origin_name)
VALUES ('GeneratorPQ', 'generator_AlphaPuPNom', 2, NULL),
       ('GeneratorPQ', 'generator_PNom', 0, 'pMax'),
       ('GeneratorPQ', 'generator_PMin', 0, 'pMin'),
       ('GeneratorPQ', 'generator_PMax', 0, 'pMax'),
       ('GeneratorPQ', 'generator_P0Pu', 0, 'p_pu'),
       ('GeneratorPQ', 'generator_Q0Pu', 0, 'q_pu'),
       ('GeneratorPQ', 'generator_U0Pu', 0, 'v_pu'),
       ('GeneratorPQ', 'generator_UPhase0', 0, 'angle_pu');

INSERT INTO models_model_parameter_definitions (model_name, parameter_definition_name, origin, origin_name)
VALUES ('GeneratorPV', 'generator_AlphaPuPNom', 2, NULL),
       ('GeneratorPV', 'generator_LambdaPuSNom', 2, NULL),
       ('GeneratorPV', 'generator_QMax', 2, NULL),
       ('GeneratorPV', 'generator_QMin', 2, NULL),
       ('GeneratorPV', 'generator_PNom', 0, 'pMax'),
       ('GeneratorPV', 'generator_SNom', 2, NULL),
       ('GeneratorPV', 'generator_PMin', 0, 'pMin'),
       ('GeneratorPV', 'generator_PMax', 0, 'pMax'),
       ('GeneratorPV', 'generator_P0Pu', 0, 'p_pu'),
       ('GeneratorPV', 'generator_Q0Pu', 0, 'q_pu'),
       ('GeneratorPV', 'generator_U0Pu', 0, 'v_pu'),
       ('GeneratorPV', 'generator_UPhase0', 0, 'angle_pu');

INSERT INTO model_parameter_definitions (name, type, created_date)
VALUES  ('load_alpha', 2, now()::timestamp),
        ('load_beta', 2, now()::timestamp),
        ('load_P0Pu', 2, now()::timestamp),
        ('load_Q0Pu', 2, now()::timestamp),
        ('load_U0Pu', 2, now()::timestamp),
        ('load_UPhase0', 2, now()::timestamp);

INSERT INTO models_model_parameter_definitions (model_name, parameter_definition_name, origin, origin_name)
VALUES ('LoadAlphaBeta', 'load_alpha', 2, NULL),
       ('LoadAlphaBeta', 'load_beta', 2, NULL),
       ('LoadAlphaBeta', 'load_P0Pu', 0, 'p_pu'),
       ('LoadAlphaBeta', 'load_Q0Pu', 0, 'q_pu'),
       ('LoadAlphaBeta', 'load_U0Pu', 0, 'v_pu'),
       ('LoadAlphaBeta', 'load_UPhase0', 0, 'angle_pu');

INSERT INTO models_model_parameter_definitions (model_name, parameter_definition_name, origin, origin_name)
VALUES ('LoadPQ', 'load_P0Pu', 0, 'p_pu'),
       ('LoadPQ', 'load_Q0Pu', 0, 'q_pu'),
       ('LoadPQ', 'load_U0Pu', 0, 'v_pu'),
       ('LoadPQ', 'load_UPhase0', 0, 'angle_pu');

INSERT INTO model_parameter_definitions (name, type, created_date)
VALUES
    ('currentLimitAutomaton_OrderToEmit', 0, now()::timestamp),
    ('currentLimitAutomaton_Running', 1, now()::timestamp),
    ('currentLimitAutomaton_IMax', 2, now()::timestamp),
    ('currentLimitAutomaton_tLagBeforeActing', 2, now()::timestamp);

INSERT INTO models_model_parameter_definitions (model_name, parameter_definition_name, origin, origin_name)
VALUES ('CurrentLimitAutomaton', 'currentLimitAutomaton_OrderToEmit', 2, NULL),
       ('CurrentLimitAutomaton', 'currentLimitAutomaton_Running', 2, NULL),
       ('CurrentLimitAutomaton', 'currentLimitAutomaton_IMax', 2, NULL),
       ('CurrentLimitAutomaton', 'currentLimitAutomaton_tLagBeforeActing', 2, NULL);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_ExcitationPu', 'GSTW', 1.),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_md', 'GSTW', 0.16),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_mq', 'GSTW', 0.16),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_nd', 'GSTW', 5.7),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_nq', 'GSTW', 5.7),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_MdPuEfd', 'GSTW', 0.),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_DPu', 'GSTW', 0.),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_H', 'GSTW', 4.975),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_RaPu', 'GSTW', 0.004),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_XlPu', 'GSTW', 0.102),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_XdPu', 'GSTW', 0.75),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_XpdPu', 'GSTW', 0.225),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_XppdPu', 'GSTW', 0.154),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_Tpd0', 'GSTW', 3.),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_Tppd0', 'GSTW', 0.04),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_XqPu', 'GSTW', 0.45),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_XppqPu', 'GSTW', 0.2),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_Tppq0', 'GSTW', 0.04),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_UNom', 'GSTW', 15.),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_PNomTurb', 'GSTW', 74.4),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_PNomAlt', 'GSTW', 74.4),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_SNom', 'GSTW', 80.),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_SnTfo', 'GSTW', 80.),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_UNomHV', 'GSTW', 15.),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_UNomLV', 'GSTW', 15.),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_UBaseHV', 'GSTW', 15.),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_UBaseLV', 'GSTW', 15.),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_RTfPu', 'GSTW', 0.),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_UseApproximation', 'GSTW', true),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_XTfPu', 'GSTW', 0.),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'URef_ValueIn', 'GSTW', 0.),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'Pm_ValueIn', 'GSTW', 0.);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name)
VALUES ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_P0Pu', 'GSTW'),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_Q0Pu', 'GSTW'),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_U0Pu', 'GSTW'),
       ('GSTW', 0, 'GeneratorSynchronousThreeWindings', 'generator_UPhase0', 'GSTW');

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_ExcitationPu', 'GSFW', 1.),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_md', 'GSFW', 0.215),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_mq', 'GSFW', 0.215),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_nd', 'GSFW', 6.995),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_nq', 'GSFW', 6.995),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_MdPuEfd', 'GSFW', 0.),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_DPu', 'GSFW', 0.),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_H', 'GSFW', 5.4),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_RaPu', 'GSFW', 0.002796),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_XlPu', 'GSFW', 0.202),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_XdPu', 'GSFW', 2.22),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_XpdPu', 'GSFW', 0.384),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_XppdPu', 'GSFW', 0.264),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_Tpd0', 'GSFW', 8.094),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_Tppd0', 'GSFW', 0.08),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_XqPu', 'GSFW', 2.22),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_XpqPu', 'GSFW', 0.393),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_XppqPu', 'GSFW', 0.262),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_Tpq0', 'GSFW', 1.572),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_Tppq0', 'GSFW', 0.084),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_UNom', 'GSFW', 24.),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_SNom', 'GSFW', 1211.),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_PNomTurb', 'GSFW', 1090.),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_PNomAlt', 'GSFW', 1090.),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_SnTfo', 'GSFW', 1211.),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_UNomHV', 'GSFW', 69.),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_UNomLV', 'GSFW', 24.),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_UBaseHV', 'GSFW', 69.),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_UBaseLV', 'GSFW', 24.),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_RTfPu', 'GSFW', 0.),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_UseApproximation', 'GSFW', true),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_XTfPu', 'GSFW', 0.1),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'URef_ValueIn', 'GSFW', 0.),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'Pm_ValueIn', 'GSFW', 0.);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name)
VALUES ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_P0Pu', 'GSFW'),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_Q0Pu', 'GSFW'),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_U0Pu', 'GSFW'),
       ('GSFW', 0, 'GeneratorSynchronousFourWindings', 'generator_UPhase0', 'GSFW');

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_ExcitationPu', 'GSTWPR', 1.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_md', 'GSTWPR', 0.16),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_mq', 'GSTWPR', 0.16),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_nd', 'GSTWPR', 5.7),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_nq', 'GSTWPR', 5.7),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_MdPuEfd', 'GSTWPR', 0.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_DPu', 'GSTWPR', 0.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_H', 'GSTWPR', 4.975),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_RaPu', 'GSTWPR', 0.004),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XlPu', 'GSTWPR', 0.102),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XdPu', 'GSTWPR', 0.75),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XpdPu', 'GSTWPR', 0.225),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XppdPu', 'GSTWPR', 0.154),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Tpd0', 'GSTWPR', 3.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Tppd0', 'GSTWPR', 0.04),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XqPu', 'GSTWPR', 0.45),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XppqPu', 'GSTWPR', 0.2),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Tppq0', 'GSTWPR', 0.04),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UNom', 'GSTWPR', 15.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_PNomTurb', 'GSTWPR', 74.4),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_PNomAlt', 'GSTWPR', 74.4),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_SNom', 'GSTWPR', 80.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_SnTfo', 'GSTWPR', 80.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UNomHV', 'GSTWPR', 15.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UNomLV', 'GSTWPR', 15.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UBaseHV', 'GSTWPR', 15.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UBaseLV', 'GSTWPR', 15.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_RTfPu', 'GSTWPR', 0.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UseApproximation', 'GSTWPR', true),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XTfPu', 'GSTWPR', 0.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_LagEfdMax', 'GSTWPR', 0.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_LagEfdMin', 'GSTWPR', 0.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_EfdMinPu', 'GSTWPR', -5.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_EfdMaxPu', 'GSTWPR', 5.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_UsRefMinPu', 'GSTWPR', 0.8),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_UsRefMaxPu', 'GSTWPR', 1.2),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_Gain', 'GSTWPR', 20.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_KGover', 'GSTWPR', 5.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_PMin', 'GSTWPR', 0.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_PMax', 'GSTWPR', 74.4),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_PNom', 'GSTWPR', 74.4),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'URef_ValueIn', 'GSTWPR', 0.),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'Pm_ValueIn', 'GSTWPR', 0.);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name)
VALUES ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_P0Pu', 'GSTWPR'),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Q0Pu', 'GSTWPR'),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_U0Pu', 'GSTWPR'),
       ('GSTWPR', 0, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UPhase0', 'GSTWPR');

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_ExcitationPu', 'GSFWPR', 1.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_md', 'GSFWPR', 0.215),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_mq', 'GSFWPR', 0.215),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_nd', 'GSFWPR', 6.995),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_nq', 'GSFWPR', 6.995),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_MdPuEfd', 'GSFWPR', 0.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_DPu', 'GSFWPR', 0.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_H', 'GSFWPR', 5.4),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_RaPu', 'GSFWPR', 0.002796),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XlPu', 'GSFWPR', 0.202),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XdPu', 'GSFWPR', 2.22),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XpdPu', 'GSFWPR', 0.384),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XppdPu', 'GSFWPR', 0.264),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tpd0', 'GSFWPR', 8.094),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tppd0', 'GSFWPR', 0.08),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XqPu', 'GSFWPR', 2.22),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XpqPu', 'GSFWPR', 0.393),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XppqPu', 'GSFWPR', 0.262),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tpq0', 'GSFWPR', 1.572),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tppq0', 'GSFWPR', 0.084),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UNom', 'GSFWPR', 24.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_SNom', 'GSFWPR', 1211.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_PNomTurb', 'GSFWPR', 1090.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_PNomAlt', 'GSFWPR', 1090.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_SnTfo', 'GSFWPR', 1211.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UNomHV', 'GSFWPR', 69.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UNomLV', 'GSFWPR', 24.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UBaseHV', 'GSFWPR', 69.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UBaseLV', 'GSFWPR', 24.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_RTfPu', 'GSFWPR', 0.0),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UseApproximation', 'GSFWPR', true),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XTfPu', 'GSFWPR', 0.1),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_LagEfdMax', 'GSFWPR', 0.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_LagEfdMin', 'GSFWPR', 0.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_EfdMinPu', 'GSFWPR', -5.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_EfdMaxPu', 'GSFWPR', 5.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_UsRefMinPu', 'GSFWPR', 0.8),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_UsRefMaxPu', 'GSFWPR', 1.2),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_Gain', 'GSFWPR', 20.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_KGover', 'GSFWPR', 5.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_PMin', 'GSFWPR', 0.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_PMax', 'GSFWPR', 1090),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_PNom', 'GSFWPR', 1090),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'URef_ValueIn', 'GSFWPR', 0.),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'Pm_ValueIn', 'GSFWPR', 0.);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name)
VALUES ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_P0Pu', 'GSFWPR'),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Q0Pu', 'GSFWPR'),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_U0Pu', 'GSFWPR'),
       ('GSFWPR', 0, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UPhase0', 'GSFWPR');

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('GPQ', 0, 'GeneratorPQ', 'generator_AlphaPuPNom', 'GPQ', 25.);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name)
VALUES ('GPQ', 0, 'GeneratorPQ', 'generator_P0Pu', 'GPQ'),
       ('GPQ', 0, 'GeneratorPQ', 'generator_PMax', 'GPQ'),
       ('GPQ', 0, 'GeneratorPQ', 'generator_PMin', 'GPQ'),
       ('GPQ', 0, 'GeneratorPQ', 'generator_PNom', 'GPQ'),
       ('GPQ', 0, 'GeneratorPQ', 'generator_Q0Pu', 'GPQ'),
       ('GPQ', 0, 'GeneratorPQ', 'generator_U0Pu', 'GPQ'),
       ('GPQ', 0, 'GeneratorPQ', 'generator_UPhase0', 'GPQ');

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('GPV', 0, 'GeneratorPV', 'generator_AlphaPuPNom', 'GPV', 20.),
       ('GPV', 0, 'GeneratorPV', 'generator_LambdaPuSNom', 'GPV', 0.01),
       ('GPV', 0, 'GeneratorPV', 'generator_QMax', 'GPV', 10.),
       ('GPV', 0, 'GeneratorPV', 'generator_QMin', 'GPV', -10.);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name)
VALUES ('GPV', 0, 'GeneratorPV', 'generator_P0Pu', 'GPV'),
       ('GPV', 0, 'GeneratorPV', 'generator_PMax', 'GPV'),
       ('GPV', 0, 'GeneratorPV', 'generator_PMin', 'GPV'),
       ('GPV', 0, 'GeneratorPV', 'generator_PNom', 'GPV'),
       ('GPV', 0, 'GeneratorPV', 'generator_Q0Pu', 'GPV'),
       ('GPV', 0, 'GeneratorPV', 'generator_SNom', 'GPV'),
       ('GPV', 0, 'GeneratorPV', 'generator_U0Pu', 'GPV'),
       ('GPV', 0, 'GeneratorPV', 'generator_UPhase0', 'GPV');

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('LAB', 0, 'LoadAlphaBeta', 'load_alpha', 'LAB', 1.),
       ('LAB', 0, 'LoadAlphaBeta', 'load_beta', 'LAB', 2.);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name)
VALUES ('LAB', 0, 'LoadAlphaBeta', 'load_P0Pu', 'LAB'),
       ('LAB', 0, 'LoadAlphaBeta', 'load_Q0Pu', 'LAB'),
       ('LAB', 0, 'LoadAlphaBeta', 'load_U0Pu', 'LAB'),
       ('LAB', 0, 'LoadAlphaBeta', 'load_UPhase0', 'LAB');

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name)
VALUES ('LPQ', 0, 'LoadPQ', 'load_P0Pu', 'LPQ'),
       ('LPQ', 0, 'LoadPQ', 'load_Q0Pu', 'LPQ'),
       ('LPQ', 0, 'LoadPQ', 'load_U0Pu', 'LPQ'),
       ('LPQ', 0, 'LoadPQ', 'load_UPhase0', 'LPQ');

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('CLA', 0, 'CurrentLimitAutomaton', 'currentLimitAutomaton_OrderToEmit', 'CLA', 1),
       ('CLA', 0, 'CurrentLimitAutomaton', 'currentLimitAutomaton_Running', 'CLA', true),
       ('CLA', 0, 'CurrentLimitAutomaton', 'currentLimitAutomaton_IMax', 'CLA', 1000.),
       ('CLA', 0, 'CurrentLimitAutomaton', 'currentLimitAutomaton_tLagBeforeActing', 'CLA', 10.);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('CLA_2_4', 0, 'CurrentLimitAutomaton', 'currentLimitAutomaton_OrderToEmit', 'CLA_2_4', 3),
       ('CLA_2_4', 0, 'CurrentLimitAutomaton', 'currentLimitAutomaton_Running', 'CLA_2_4', true),
       ('CLA_2_4', 0, 'CurrentLimitAutomaton', 'currentLimitAutomaton_IMax', 'CLA_2_4', 1000.),
       ('CLA_2_4', 0, 'CurrentLimitAutomaton', 'currentLimitAutomaton_tLagBeforeActing', 'CLA_2_4', 10.);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('CLA_2_5', 0, 'CurrentLimitAutomaton', 'currentLimitAutomaton_OrderToEmit', 'CLA_2_5', 1),
       ('CLA_2_5', 0, 'CurrentLimitAutomaton', 'currentLimitAutomaton_Running', 'CLA_2_5', true),
       ('CLA_2_5', 0, 'CurrentLimitAutomaton', 'currentLimitAutomaton_IMax', 'CLA_2_5', 600.),
       ('CLA_2_5', 0, 'CurrentLimitAutomaton', 'currentLimitAutomaton_tLagBeforeActing', 'CLA_2_5', 5.);

-- Update parameters for IEEE14 model

INSERT INTO model_sets_group (model_name, name, type)
VALUES ('GeneratorSynchronousFourWindingsProportionalRegulations', 'IEEE14', 1);

INSERT INTO model_parameter_sets (group_name, group_type, model_name, name)
VALUES ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'IEEE14_GEN____1_SM');

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_ExcitationPu', 'IEEE14_GEN____1_SM', 1),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_md', 'IEEE14_GEN____1_SM', 0.215),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_mq', 'IEEE14_GEN____1_SM', 0.215),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_nd', 'IEEE14_GEN____1_SM', 6.995),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_nq', 'IEEE14_GEN____1_SM', 6.995),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_MdPuEfd', 'IEEE14_GEN____1_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_DPu', 'IEEE14_GEN____1_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_H', 'IEEE14_GEN____1_SM', 5.4),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_RaPu', 'IEEE14_GEN____1_SM', 0.002796),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XlPu', 'IEEE14_GEN____1_SM', 0.202),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XdPu', 'IEEE14_GEN____1_SM', 2.22),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XpdPu', 'IEEE14_GEN____1_SM', 0.384),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XppdPu', 'IEEE14_GEN____1_SM', 0.264),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tpd0', 'IEEE14_GEN____1_SM', 8.094),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tppd0', 'IEEE14_GEN____1_SM', 0.08),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XqPu', 'IEEE14_GEN____1_SM', 2.22),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XpqPu', 'IEEE14_GEN____1_SM', 0.393),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XppqPu', 'IEEE14_GEN____1_SM', 0.262),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tpq0', 'IEEE14_GEN____1_SM', 1.572),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tppq0', 'IEEE14_GEN____1_SM', 0.084),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UNom', 'IEEE14_GEN____1_SM', 24),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_SNom', 'IEEE14_GEN____1_SM', 1211),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_PNomTurb', 'IEEE14_GEN____1_SM', 1090),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_PNomAlt', 'IEEE14_GEN____1_SM', 1090),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_SnTfo', 'IEEE14_GEN____1_SM', 1211),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UNomHV', 'IEEE14_GEN____1_SM', 69),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UNomLV', 'IEEE14_GEN____1_SM', 24),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UBaseHV', 'IEEE14_GEN____1_SM', 69),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UBaseLV', 'IEEE14_GEN____1_SM', 24),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_RTfPu', 'IEEE14_GEN____1_SM', 0.0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UseApproximation', 'IEEE14_GEN____1_SM', true),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XTfPu', 'IEEE14_GEN____1_SM', 0.1),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_LagEfdMax', 'IEEE14_GEN____1_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_LagEfdMin', 'IEEE14_GEN____1_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_EfdMinPu', 'IEEE14_GEN____1_SM', -5),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_EfdMaxPu', 'IEEE14_GEN____1_SM', 5),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_UsRefMinPu', 'IEEE14_GEN____1_SM', 0.8),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_UsRefMaxPu', 'IEEE14_GEN____1_SM', 1.2),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_Gain', 'IEEE14_GEN____1_SM', 20),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_KGover', 'IEEE14_GEN____1_SM', 5),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_PMin', 'IEEE14_GEN____1_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_PMax', 'IEEE14_GEN____1_SM', 1090),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_PNom', 'IEEE14_GEN____1_SM', 1090),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'URef_ValueIn', 'IEEE14_GEN____1_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'Pm_ValueIn', 'IEEE14_GEN____1_SM', 0);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name)
VALUES ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_P0Pu', 'IEEE14_GEN____1_SM'),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Q0Pu', 'IEEE14_GEN____1_SM'),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_U0Pu', 'IEEE14_GEN____1_SM'),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UPhase0', 'IEEE14_GEN____1_SM');

INSERT INTO model_parameter_sets (group_name, group_type, model_name, name)
VALUES ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'IEEE14_GEN____2_SM');

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_ExcitationPu', 'IEEE14_GEN____2_SM', 1),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_md', 'IEEE14_GEN____2_SM', 0.084),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_mq', 'IEEE14_GEN____2_SM', 0.084),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_nd', 'IEEE14_GEN____2_SM', 5.57),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_nq', 'IEEE14_GEN____2_SM', 5.57),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_MdPuEfd', 'IEEE14_GEN____2_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_DPu', 'IEEE14_GEN____2_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_H', 'IEEE14_GEN____2_SM', 6.3),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_RaPu', 'IEEE14_GEN____2_SM', 0.00357),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XlPu', 'IEEE14_GEN____2_SM', 0.219),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XdPu', 'IEEE14_GEN____2_SM', 2.57),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XpdPu', 'IEEE14_GEN____2_SM', 0.407),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XppdPu', 'IEEE14_GEN____2_SM', 0.3),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tpd0', 'IEEE14_GEN____2_SM', 9.651),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tppd0', 'IEEE14_GEN____2_SM', 0.058),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XqPu', 'IEEE14_GEN____2_SM', 2.57),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XpqPu', 'IEEE14_GEN____2_SM', 0.454),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XppqPu', 'IEEE14_GEN____2_SM', 0.301),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tpq0', 'IEEE14_GEN____2_SM', 1.009),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tppq0', 'IEEE14_GEN____2_SM', 0.06),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UNom', 'IEEE14_GEN____2_SM', 24),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_SNom', 'IEEE14_GEN____2_SM', 1120),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_PNomTurb', 'IEEE14_GEN____2_SM', 1008),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_PNomAlt', 'IEEE14_GEN____2_SM', 1008),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_SnTfo', 'IEEE14_GEN____2_SM', 1120),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UNomHV', 'IEEE14_GEN____2_SM', 69),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UNomLV', 'IEEE14_GEN____2_SM', 24),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UBaseHV', 'IEEE14_GEN____2_SM', 69),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UBaseLV', 'IEEE14_GEN____2_SM', 24),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_RTfPu', 'IEEE14_GEN____2_SM', 0.0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UseApproximation', 'IEEE14_GEN____2_SM', true),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XTfPu', 'IEEE14_GEN____2_SM', 0.1),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_LagEfdMax', 'IEEE14_GEN____2_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_LagEfdMin', 'IEEE14_GEN____2_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_EfdMinPu', 'IEEE14_GEN____2_SM', -5),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_EfdMaxPu', 'IEEE14_GEN____2_SM', 5),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_UsRefMinPu', 'IEEE14_GEN____2_SM', 0.8),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_UsRefMaxPu', 'IEEE14_GEN____2_SM', 1.2),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_Gain', 'IEEE14_GEN____2_SM', 20),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_KGover', 'IEEE14_GEN____2_SM', 5),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_PMin', 'IEEE14_GEN____2_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_PMax', 'IEEE14_GEN____2_SM', 1008),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_PNom', 'IEEE14_GEN____2_SM', 1008),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'URef_ValueIn', 'IEEE14_GEN____2_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'Pm_ValueIn', 'IEEE14_GEN____2_SM', 0);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name)
VALUES ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_P0Pu', 'IEEE14_GEN____2_SM'),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Q0Pu', 'IEEE14_GEN____2_SM'),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_U0Pu', 'IEEE14_GEN____2_SM'),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UPhase0', 'IEEE14_GEN____2_SM');

INSERT INTO model_parameter_sets (group_name, group_type, model_name, name)
VALUES ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'IEEE14_GEN____3_SM');

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_ExcitationPu', 'IEEE14_GEN____3_SM', 1),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_md', 'IEEE14_GEN____3_SM', 0.05),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_mq', 'IEEE14_GEN____3_SM', 0.05),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_nd', 'IEEE14_GEN____3_SM', 9.285),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_nq', 'IEEE14_GEN____3_SM', 9.285),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_MdPuEfd', 'IEEE14_GEN____3_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_DPu', 'IEEE14_GEN____3_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_H', 'IEEE14_GEN____3_SM', 5.625),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_RaPu', 'IEEE14_GEN____3_SM', 0.00316),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XlPu', 'IEEE14_GEN____3_SM', 0.256),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XdPu', 'IEEE14_GEN____3_SM', 2.81),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XpdPu', 'IEEE14_GEN____3_SM', 0.509),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XppdPu', 'IEEE14_GEN____3_SM', 0.354),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tpd0', 'IEEE14_GEN____3_SM', 10.041),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tppd0', 'IEEE14_GEN____3_SM', 0.065),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XqPu', 'IEEE14_GEN____3_SM', 2.62),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XpqPu', 'IEEE14_GEN____3_SM', 0.601),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XppqPu', 'IEEE14_GEN____3_SM', 0.377),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tpq0', 'IEEE14_GEN____3_SM', 1.22),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Tppq0', 'IEEE14_GEN____3_SM', 0.094),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UNom', 'IEEE14_GEN____3_SM', 20),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_PNomTurb', 'IEEE14_GEN____3_SM', 1485),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_PNomAlt', 'IEEE14_GEN____3_SM', 1485),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_SNom', 'IEEE14_GEN____3_SM', 1650),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_SnTfo', 'IEEE14_GEN____3_SM', 1650),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UNomHV', 'IEEE14_GEN____3_SM', 69),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UNomLV', 'IEEE14_GEN____3_SM', 20),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UBaseHV', 'IEEE14_GEN____3_SM', 69),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UBaseLV', 'IEEE14_GEN____3_SM', 20),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_RTfPu', 'IEEE14_GEN____3_SM', 0.0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UseApproximation', 'IEEE14_GEN____3_SM', true),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_XTfPu', 'IEEE14_GEN____3_SM', 0.1),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_LagEfdMax', 'IEEE14_GEN____3_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_LagEfdMin', 'IEEE14_GEN____3_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_EfdMinPu', 'IEEE14_GEN____3_SM', -5),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_EfdMaxPu', 'IEEE14_GEN____3_SM', 5),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_UsRefMinPu', 'IEEE14_GEN____3_SM', 0.8),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_UsRefMaxPu', 'IEEE14_GEN____3_SM', 1.2),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'voltageRegulator_Gain', 'IEEE14_GEN____3_SM', 20),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_KGover', 'IEEE14_GEN____3_SM', 5),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_PMin', 'IEEE14_GEN____3_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_PMax', 'IEEE14_GEN____3_SM', 1485),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'governor_PNom', 'IEEE14_GEN____3_SM', 1485),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'URef_ValueIn', 'IEEE14_GEN____3_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'Pm_ValueIn', 'IEEE14_GEN____3_SM', 0);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name)
VALUES ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_P0Pu', 'IEEE14_GEN____3_SM'),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_Q0Pu', 'IEEE14_GEN____3_SM'),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_U0Pu', 'IEEE14_GEN____3_SM'),
       ('IEEE14', 1, 'GeneratorSynchronousFourWindingsProportionalRegulations', 'generator_UPhase0', 'IEEE14_GEN____3_SM');

INSERT INTO model_sets_group (model_name, name, type)
VALUES ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'IEEE14', 1);

INSERT INTO model_parameter_sets (group_name, group_type, model_name, name)
VALUES ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'IEEE14_GEN____6_SM');

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_ExcitationPu', 'IEEE14_GEN____6_SM', 1),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_md', 'IEEE14_GEN____6_SM', 0.16),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_mq', 'IEEE14_GEN____6_SM', 0.16),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_nd', 'IEEE14_GEN____6_SM', 5.7),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_nq', 'IEEE14_GEN____6_SM', 5.7),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_MdPuEfd', 'IEEE14_GEN____6_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_DPu', 'IEEE14_GEN____6_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_H', 'IEEE14_GEN____6_SM', 4.975),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_RaPu', 'IEEE14_GEN____6_SM', 0.004),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XlPu', 'IEEE14_GEN____6_SM', 0.102),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XdPu', 'IEEE14_GEN____6_SM', 0.75),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XpdPu', 'IEEE14_GEN____6_SM', 0.225),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XppdPu', 'IEEE14_GEN____6_SM', 0.154),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Tpd0', 'IEEE14_GEN____6_SM', 3),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Tppd0', 'IEEE14_GEN____6_SM', 0.04),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XqPu', 'IEEE14_GEN____6_SM', 0.45),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XppqPu', 'IEEE14_GEN____6_SM', 0.2),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Tppq0', 'IEEE14_GEN____6_SM', 0.04),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UNom', 'IEEE14_GEN____6_SM', 15),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_PNomTurb', 'IEEE14_GEN____6_SM', 74.4),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_PNomAlt', 'IEEE14_GEN____6_SM', 74.4),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_SNom', 'IEEE14_GEN____6_SM', 80),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_SnTfo', 'IEEE14_GEN____6_SM', 80),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UNomHV', 'IEEE14_GEN____6_SM', 15),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UNomLV', 'IEEE14_GEN____6_SM', 15),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UBaseHV', 'IEEE14_GEN____6_SM', 15),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UBaseLV', 'IEEE14_GEN____6_SM', 15),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_RTfPu', 'IEEE14_GEN____6_SM', 0.0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UseApproximation', 'IEEE14_GEN____6_SM', true),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XTfPu', 'IEEE14_GEN____6_SM', 0.0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_LagEfdMax', 'IEEE14_GEN____6_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_LagEfdMin', 'IEEE14_GEN____6_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_EfdMinPu', 'IEEE14_GEN____6_SM', -5),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_EfdMaxPu', 'IEEE14_GEN____6_SM', 5),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_UsRefMinPu', 'IEEE14_GEN____6_SM', 0.8),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_UsRefMaxPu', 'IEEE14_GEN____6_SM', 1.2),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_Gain', 'IEEE14_GEN____6_SM', 20),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_KGover', 'IEEE14_GEN____6_SM', 5),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_PMin', 'IEEE14_GEN____6_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_PMax', 'IEEE14_GEN____6_SM', 74.4),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_PNom', 'IEEE14_GEN____6_SM', 74.4),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'URef_ValueIn', 'IEEE14_GEN____6_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'Pm_ValueIn', 'IEEE14_GEN____6_SM', 0);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name)
VALUES ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_P0Pu', 'IEEE14_GEN____6_SM'),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Q0Pu', 'IEEE14_GEN____6_SM'),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_U0Pu', 'IEEE14_GEN____6_SM'),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UPhase0', 'IEEE14_GEN____6_SM');

INSERT INTO model_parameter_sets (group_name, group_type, model_name, name)
VALUES ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'IEEE14_GEN____8_SM');

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_ExcitationPu', 'IEEE14_GEN____8_SM', 1),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_md', 'IEEE14_GEN____8_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_mq', 'IEEE14_GEN____8_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_nd', 'IEEE14_GEN____8_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_nq', 'IEEE14_GEN____8_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_MdPuEfd', 'IEEE14_GEN____8_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_DPu', 'IEEE14_GEN____8_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_H', 'IEEE14_GEN____8_SM', 2.748),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_RaPu', 'IEEE14_GEN____8_SM', 0.004),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XlPu', 'IEEE14_GEN____8_SM', 0.11),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XdPu', 'IEEE14_GEN____8_SM', 1.53),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XpdPu', 'IEEE14_GEN____8_SM', 0.31),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XppdPu', 'IEEE14_GEN____8_SM', 0.275),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Tpd0', 'IEEE14_GEN____8_SM', 8.4),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Tppd0', 'IEEE14_GEN____8_SM', 0.096),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XqPu', 'IEEE14_GEN____8_SM', 0.99),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XppqPu', 'IEEE14_GEN____8_SM', 0.58),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Tppq0', 'IEEE14_GEN____8_SM', 0.56),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UNom', 'IEEE14_GEN____8_SM', 18),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_PNomTurb', 'IEEE14_GEN____8_SM', 228),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_PNomAlt', 'IEEE14_GEN____8_SM', 228),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_SNom', 'IEEE14_GEN____8_SM', 250),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_SnTfo', 'IEEE14_GEN____8_SM', 250),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UNomHV', 'IEEE14_GEN____8_SM', 13.8),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UNomLV', 'IEEE14_GEN____8_SM', 18),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UBaseHV', 'IEEE14_GEN____8_SM', 13.8),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UBaseLV', 'IEEE14_GEN____8_SM', 18),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_RTfPu', 'IEEE14_GEN____8_SM', 0.0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UseApproximation', 'IEEE14_GEN____8_SM', true),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_XTfPu', 'IEEE14_GEN____8_SM', 0.1),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_LagEfdMax', 'IEEE14_GEN____8_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_LagEfdMin', 'IEEE14_GEN____8_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_EfdMinPu', 'IEEE14_GEN____8_SM', -5),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_EfdMaxPu', 'IEEE14_GEN____8_SM', 5),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_UsRefMinPu', 'IEEE14_GEN____8_SM', 0.8),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_UsRefMaxPu', 'IEEE14_GEN____8_SM', 1.2),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'voltageRegulator_Gain', 'IEEE14_GEN____8_SM', 20),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_KGover', 'IEEE14_GEN____8_SM', 5),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_PMin', 'IEEE14_GEN____8_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_PMax', 'IEEE14_GEN____8_SM', 228),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'governor_PNom', 'IEEE14_GEN____8_SM', 228),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'URef_ValueIn', 'IEEE14_GEN____8_SM', 0),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'Pm_ValueIn', 'IEEE14_GEN____8_SM', 0);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name)
VALUES ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_P0Pu', 'IEEE14_GEN____8_SM'),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_Q0Pu', 'IEEE14_GEN____8_SM'),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_U0Pu', 'IEEE14_GEN____8_SM'),
       ('IEEE14', 1, 'GeneratorSynchronousThreeWindingsProportionalRegulations', 'generator_UPhase0', 'IEEE14_GEN____8_SM');

-- variables for LoadAlphaBeta model
INSERT INTO model_variable_definitions (variable_definition_name, type, unit, factor, created_date)
VALUES ('load_PPu', 2, 'MW', 100, now()::timestamp),
       ('load_PRefPu', 2, 'MW', 100, now()::timestamp),
       ('load_QPu', 2, 'MW', 100, now()::timestamp),
       ('load_QRefPu', 2, 'MW', 100, now()::timestamp),
       ('load_running_value', 1, NULL, NULL, now()::timestamp);

INSERT INTO models_model_variable_definitions (model_name, variable_definition_name)
VALUES ('LoadAlphaBeta', 'load_PPu'),
       ('LoadAlphaBeta', 'load_PRefPu'),
       ('LoadAlphaBeta', 'load_QPu'),
       ('LoadAlphaBeta', 'load_QRefPu'),
       ('LoadAlphaBeta', 'load_running_value');

-- variable sets for three/four windings generator models
INSERT INTO model_variable_sets (variable_set_name, created_date)
VALUES ('Generator', now()::timestamp),
       ('VoltageRegulator', now()::timestamp);

-- variables grouped in sets used in three/four windings generator models
INSERT INTO model_variable_definitions (variable_definition_name, type, unit, factor, created_date)
VALUES ('generator_omegaPu', 2, 'pu', NULL, now()::timestamp),
       ('generator_PGen', 2, 'MW', NULL, now()::timestamp),
       ('generator_QGen', 2, 'MW', NULL, now()::timestamp),
       ('generator_UStatorPu', 2, 'pu', NULL, now()::timestamp);

INSERT INTO model_variable_sets_model_variable_definitions (variable_definition_name, variable_set_name)
VALUES ('generator_omegaPu', 'Generator'),
       ('generator_PGen', 'Generator'),
       ('generator_QGen', 'Generator'),
       ('generator_UStatorPu', 'Generator');

INSERT INTO model_variable_definitions (variable_definition_name, type, unit, factor, created_date)
VALUES ('voltageRegulator_EfdPu', 2, 'pu', NULL, now()::timestamp);

INSERT INTO model_variable_sets_model_variable_definitions (variable_definition_name, variable_set_name)
VALUES ('voltageRegulator_EfdPu', 'VoltageRegulator');

-- variables sets for GeneratorSynchronousThreeWindings model
INSERT INTO models_model_variable_sets (model_name, variable_set_name)
VALUES ('GeneratorSynchronousThreeWindings', 'Generator'),
       ('GeneratorSynchronousThreeWindings', 'VoltageRegulator');

-- variables sets for GeneratorSynchronousFourWindings model
INSERT INTO models_model_variable_sets (model_name, variable_set_name)
VALUES ('GeneratorSynchronousFourWindings', 'Generator'),
       ('GeneratorSynchronousFourWindings', 'VoltageRegulator');

-- variables sets for GeneratorSynchronousThreeWindingsProportionalRegulations model
INSERT INTO models_model_variable_sets (model_name, variable_set_name)
VALUES ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'Generator'),
       ('GeneratorSynchronousThreeWindingsProportionalRegulations', 'VoltageRegulator');

-- variables sets for GeneratorSynchronousFourWindingsProportionalRegulations model
INSERT INTO models_model_variable_sets (model_name, variable_set_name)
VALUES ('GeneratorSynchronousFourWindingsProportionalRegulations', 'Generator'),
       ('GeneratorSynchronousFourWindingsProportionalRegulations', 'VoltageRegulator');

-- variable sets for PQ/PV windings generator models
INSERT INTO model_variable_sets (variable_set_name, created_date)
VALUES ('GeneratorPQ', now()::timestamp),
       ('GeneratorPV', now()::timestamp);

-- variables grouped in sets used in PQ/PV generator models
INSERT INTO model_variable_definitions (variable_definition_name, type, unit, factor, created_date)
VALUES ('generator_running_value', 1, NULL, NULL, now()::timestamp),
       ('generator_QGenPu', 2, 'Mvar', 100, now()::timestamp),
       ('generator_PGenPu', 2, 'MW', 100, now()::timestamp);

INSERT INTO model_variable_sets_model_variable_definitions (variable_definition_name, variable_set_name)
VALUES ('generator_running_value', 'GeneratorPQ'),
       ('generator_QGenPu', 'GeneratorPQ'),
       ('generator_PGenPu', 'GeneratorPQ'),
       ('generator_running_value', 'GeneratorPV'),
       ('generator_QGenPu', 'GeneratorPV'),
       ('generator_PGenPu', 'GeneratorPV');

-- variables sets for GeneratorPQ/GeneratorPV models
INSERT INTO models_model_variable_sets (model_name, variable_set_name)
VALUES ('GeneratorPQ', 'GeneratorPQ'),
       ('GeneratorPV', 'GeneratorPV');

--- model TapChangerBlockingAutomaton for VOLTAGE Equipment type
INSERT INTO models (model_name, equipment_type, created_date)
VALUES ('TapChangerBlockingAutomaton', 3, now()::timestamp);

INSERT INTO model_sets_group (model_name, name, type)
VALUES ('TapChangerBlockingAutomaton', 'TCB', 0),
       ('TapChangerBlockingAutomaton', 'TCB_2_4', 0),
       ('TapChangerBlockingAutomaton', 'TCB_2_5', 0);

INSERT INTO model_parameter_sets (group_name, group_type, model_name, name)
VALUES ('TCB', 0, 'TapChangerBlockingAutomaton', 'TCB'),
       ('TCB_2_4', 0, 'TapChangerBlockingAutomaton', 'TCB_2_4'),
       ('TCB_2_5', 0, 'TapChangerBlockingAutomaton', 'TCB_2_5');

INSERT INTO model_parameter_definitions (name, type, fixed_value, created_date)
VALUES ('tapChangerBlocking_UMin', 2, NULL, now()::timestamp),
       ('tapChangerBlocking_tLagBeforeBlocked', 2, NULL, now()::timestamp),
       ('tapChangerBlocking_tLagTransBlockedD', 2, NULL, now()::timestamp),
       ('tapChangerBlocking_tLagTransBlockedT', 2, NULL, now()::timestamp);

INSERT INTO models_model_parameter_definitions (model_name, parameter_definition_name, origin, origin_name)
VALUES ('TapChangerBlockingAutomaton', 'tapChangerBlocking_UMin', 2, NULL),
       ('TapChangerBlockingAutomaton', 'tapChangerBlocking_tLagBeforeBlocked', 2, NULL),
       ('TapChangerBlockingAutomaton', 'tapChangerBlocking_tLagTransBlockedD', 2, NULL),
       ('TapChangerBlockingAutomaton', 'tapChangerBlocking_tLagTransBlockedT', 2, NULL);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('TCB', 0, 'TapChangerBlockingAutomaton', 'tapChangerBlocking_UMin', 'TCB', 10.),
       ('TCB', 0, 'TapChangerBlockingAutomaton', 'tapChangerBlocking_tLagBeforeBlocked', 'TCB', 100.),
       ('TCB', 0, 'TapChangerBlockingAutomaton', 'tapChangerBlocking_tLagTransBlockedD', 'TCB', 1000.),
       ('TCB', 0, 'TapChangerBlockingAutomaton', 'tapChangerBlocking_tLagTransBlockedT', 'TCB', 10.);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('TCB_2_4', 0, 'TapChangerBlockingAutomaton', 'tapChangerBlocking_UMin', 'TCB_2_4', 14.),
       ('TCB_2_4', 0, 'TapChangerBlockingAutomaton', 'tapChangerBlocking_tLagBeforeBlocked', 'TCB_2_4', 104.),
       ('TCB_2_4', 0, 'TapChangerBlockingAutomaton', 'tapChangerBlocking_tLagTransBlockedD', 'TCB_2_4', 1004.),
       ('TCB_2_4', 0, 'TapChangerBlockingAutomaton', 'tapChangerBlocking_tLagTransBlockedT', 'TCB_2_4', 14.);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('TCB_2_5', 0, 'TapChangerBlockingAutomaton', 'tapChangerBlocking_UMin', 'TCB_2_5', 15.),
       ('TCB_2_5', 0, 'TapChangerBlockingAutomaton', 'tapChangerBlocking_tLagBeforeBlocked', 'TCB_2_5', 105.),
       ('TCB_2_5', 0, 'TapChangerBlockingAutomaton', 'tapChangerBlocking_tLagTransBlockedD', 'TCB_2_5', 1005.),
       ('TCB_2_5', 0, 'TapChangerBlockingAutomaton', 'tapChangerBlocking_tLagTransBlockedT', 'TCB_2_5', 15.);

--- model StaticVarCompensator for STATIC_VAR_COMPENSATOR Equipment type
INSERT INTO models (model_name, equipment_type, created_date)
VALUES ('StaticVarCompensator', 9, now()::timestamp);

INSERT INTO model_sets_group (model_name, name, type)
VALUES ('StaticVarCompensator', 'SVarCT', 0);

INSERT INTO model_parameter_sets (group_name, group_type, model_name, name)
VALUES ('SVarCT', 0, 'StaticVarCompensator', 'SVarCT');

INSERT INTO model_parameter_definitions (name, origin_name, type, fixed_value, created_date)
VALUES ('SVarC_BMaxPu', NULL, 2, NULL, now()::timestamp),
       ('SVarC_BMinPu', NULL, 2, NULL, now()::timestamp),
       ('SVarC_BShuntPu', NULL, 2, NULL, now()::timestamp),
       ('SVarC_IMaxPu', NULL, 2, NULL, now()::timestamp),
       ('SVarC_IMinPu', NULL, 2, NULL, now()::timestamp),
       ('SVarC_KCurrentLimiter', NULL, 2, NULL, now()::timestamp),
       ('SVarC_Kp', NULL, 2, NULL, now()::timestamp),
       ('SVarC_Lambda', NULL, 2, NULL, now()::timestamp),
       ('SVarC_Mode0', 'regulatingMode', 0, NULL, now()::timestamp),
       ('SVarC_P0Pu', 'p_pu', 2, NULL, now()::timestamp),
       ('SVarC_Q0Pu', 'q_pu', 2, NULL, now()::timestamp),
       ('SVarC_SNom', NULL, 2, NULL, now()::timestamp),
       ('SVarC_Ti', NULL, 2, NULL, now()::timestamp),
       ('SVarC_U0Pu', 'v_pu', 2, NULL, now()::timestamp),
       ('SVarC_UBlock', NULL, 2, NULL, now()::timestamp),
       ('SVarC_UNom', NULL, 2, NULL, now()::timestamp),
       ('SVarC_UPhase0', 'angle_pu', 2, NULL, now()::timestamp),
       ('SVarC_URefDown', NULL, 2, NULL, now()::timestamp),
       ('SVarC_URefUp', NULL, 2, NULL, now()::timestamp),
       ('SVarC_UThresholdDown', NULL, 2, NULL, now()::timestamp),
       ('SVarC_UThresholdUp', NULL, 2, NULL, now()::timestamp),
       ('SVarC_UUnblockDown', NULL, 2, NULL, now()::timestamp),
       ('SVarC_UUnblockUp', NULL, 2, NULL, now()::timestamp),
       ('SVarC_tThresholdDown', NULL, 2, NULL, now()::timestamp),
       ('SVarC_tThresholdUp', NULL, 2, NULL, now()::timestamp);

INSERT INTO models_model_parameter_definitions (model_name, parameter_definition_name, origin)
VALUES ('StaticVarCompensator', 'SVarC_BMaxPu', 2),
       ('StaticVarCompensator', 'SVarC_BMinPu', 2),
       ('StaticVarCompensator', 'SVarC_BShuntPu', 2),
       ('StaticVarCompensator', 'SVarC_IMaxPu', 2),
       ('StaticVarCompensator', 'SVarC_IMinPu', 2),
       ('StaticVarCompensator', 'SVarC_KCurrentLimiter', 2),
       ('StaticVarCompensator', 'SVarC_Kp', 2),
       ('StaticVarCompensator', 'SVarC_Lambda', 2),
       ('StaticVarCompensator', 'SVarC_Mode0', 2),
       ('StaticVarCompensator', 'SVarC_P0Pu', 0),
       ('StaticVarCompensator', 'SVarC_Q0Pu', 0),
       ('StaticVarCompensator', 'SVarC_SNom', 2),
       ('StaticVarCompensator', 'SVarC_Ti', 2),
       ('StaticVarCompensator', 'SVarC_U0Pu', 0),
       ('StaticVarCompensator', 'SVarC_UBlock', 2),
       ('StaticVarCompensator', 'SVarC_UNom', 2),
       ('StaticVarCompensator', 'SVarC_UPhase0', 0),
       ('StaticVarCompensator', 'SVarC_URefDown', 2),
       ('StaticVarCompensator', 'SVarC_URefUp', 2),
       ('StaticVarCompensator', 'SVarC_UThresholdDown', 2),
       ('StaticVarCompensator', 'SVarC_UThresholdUp', 2),
       ('StaticVarCompensator', 'SVarC_UUnblockDown', 2),
       ('StaticVarCompensator', 'SVarC_UUnblockUp', 2),
       ('StaticVarCompensator', 'SVarC_tThresholdDown', 2),
       ('StaticVarCompensator', 'SVarC_tThresholdUp', 2);

INSERT INTO model_parameters (group_name, group_type, model_name, name, set_name, value_)
VALUES ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_BMaxPu', 'SVarCT', 1.0678),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_BMinPu', 'SVarCT', -1.0466),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_BShuntPu', 'SVarCT', 0),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_IMaxPu', 'SVarCT', 1),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_IMinPu', 'SVarCT', -1),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_KCurrentLimiter', 'SVarCT', 8),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_Kp', 'SVarCT', 1.75),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_Lambda', 'SVarCT', 0.01),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_Mode0', 'SVarCT', 2),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_UNom', 'SVarCT', 250),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_Ti', 'SVarCT', 0.003428),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_UBlock', 'SVarCT', 5),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_SNom', 'SVarCT', 225),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_URefDown', 'SVarCT', 220),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_URefUp', 'SVarCT', 230),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_UThresholdDown', 'SVarCT', 218),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_UThresholdUp', 'SVarCT', 240),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_UUnblockDown', 'SVarCT', 180),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_UUnblockUp', 'SVarCT', 270),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_tThresholdDown', 'SVarCT', 0),
       ('SVarCT', 0, 'StaticVarCompensator', 'SVarC_tThresholdUp', 'SVarCT', 60);

INSERT INTO model_variable_definitions (variable_definition_name, type, unit, factor, created_date)
VALUES ('SVarC_injector_UPu', 2, NULL, NULL, now()::timestamp),
       ('SVarC_injector_PInjPu', 2, NULL, NULL, now()::timestamp),
       ('SVarC_injector_QInjPu', 2, NULL, NULL, now()::timestamp),
       ('SVarC_injector_BPu', 2, NULL, NULL, now()::timestamp),
       ('SVarC_modeHandling_mode_value', 0, NULL, NULL, now()::timestamp);

INSERT INTO models_model_variable_definitions (model_name, variable_definition_name)
VALUES ('StaticVarCompensator', 'SVarC_injector_UPu'),
       ('StaticVarCompensator', 'SVarC_injector_PInjPu'),
       ('StaticVarCompensator', 'SVarC_injector_QInjPu'),
       ('StaticVarCompensator', 'SVarC_injector_BPu'),
       ('StaticVarCompensator', 'SVarC_modeHandling_mode_value');