
for (<equipmentType> eqpt : network.<collectionName>) {
     <rules; separator=expr>

     if (<composition>) {
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

    <if(equipmentType && equipmentType === "Generator">
    //
    OmegaRef {
        generatorDynamicModelId eqpt.id
    }
    <endif>
}