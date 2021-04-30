    if (eqpt.id != "_GEN____6_SM" && eqpt.id != "_GEN____8_SM") {
        GeneratorSynchronousFourWindingsProportionalRegulations {
            staticId eqpt.id
            parameterSetId "GSFWPR" + eqpt.id
        }
    } else {
        <model.name> {
            staticId eqpt.id
            parameterSetId <model.parameterSetId>
        }
    }