
for (<equipmentType> eqpt : network.<collectionName>) {
     <rules; separator="\n } else ">
    }

    <if(equipmentType && equipmentType === "Generator">
    //
    OmegaRef {
        generatorDynamicModelId eqpt.id
    }
    <endif>
}