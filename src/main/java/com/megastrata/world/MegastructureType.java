package com.megastrata.world;

public enum MegastructureType {
    ENDLESS_FRAME("endless_frame", -1200, 1800, StructureModel.INFINITE),          // I. Нескончаемый каркас + Панельный Гигахрущ
    VERTICAL_HORIZON_CITY("vertical_city", -500, 1500, StructureModel.CLOSED),     // II. Город вертикального горизонта (Коулун / Панельки)
    DEAD_OCEAN("dead_ocean", -1400, -200, StructureModel.CLOSED),                  // III. Мёртвый океан
    INDUSTRIAL_CONTINENT("industrial_continent", -1000, 600, StructureModel.INFINITE), // IV. Промышленный материк и трубопроводы
    BIOMECHANICAL_FOREST("biomech_forest", -800, 400, StructureModel.FRAGMENTED),  // V. Биомеханический лес
    HORIZONTAL_SKIES("horizontal_skies", 800, 2000, StructureModel.CLOSED),        // VI. Горизонтальные небеса и висячие панельки
    BURIED_ARCHIVE("buried_archive", -1600, -600, StructureModel.FRAGMENTED),      // VII. Архив погребённых машин и лабиринт
    ZERO_LAYER("zero_layer", -2000, -1400, StructureModel.INFINITE);               // VIII. Нулевой слой

    public enum StructureModel { INFINITE, CLOSED, FRAGMENTED }

    public final String id;
    public final int minY;
    public final int maxY;
    public final StructureModel model;

    MegastructureType(String id, int minY, int maxY, StructureModel model) {
        this.id = id;
        this.minY = minY;
        this.maxY = maxY;
        this.model = model;
    }
}
