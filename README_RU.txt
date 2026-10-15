========================================================================
  LIMINAL WORLD MEGASTRUCTURE — FORGE 1.20.1
========================================================================

ЧТО ВНУТРИ АРХИВА:
Полностью собранный воедино проект мода для Minecraft Forge 1.20.1:
- LiminalWorldMod.java (главный класс + спавн игрока внутри этажей на Y=67)
- MegastructureGenerator.java (генератор этажей, атриумов, лестниц, тупиков и тех. перекрытий для модовых руд)
- LiminalLootPlacer.java (спавн паллет с припасами, бочек и модовых сундуков в тупиках)
- LootCompatibilityHandler.java (инъекция лута и совместимость с GlobalLootModifiers)
- NoiseBasedChunkGeneratorMixin.java (перехват генерации мира с сохранением биомов Terrablender/BOP/Alex's Caves)
- 3 таблицы лута на Forge-тегах (level_0_supplies, poolrooms_supplies, deep_brutalist_supplies)

ПОЧЕМУ НУЖЕН 1 ШАГ СБОРКИ В .JAR:
Моды с Mixin на Forge 1.20.1 при сборке должны пройти автоматическую реобфускацию (reobfJar)
под внутренние таблицы имен Minecraft 1.20.1 (SRG mappings), которые скачиваются с серверов Mojang/Forge.

КАК ПОЛУЧИТЬ ГОТОВЫЙ .JAR ФАЙЛ ЗА 1 КЛИК:
Способ А (На ПК с Windows):
1. Распакуйте этот архив в любую папку.
2. Запустите файл BUILD_MOD_WINDOWS.bat (требуется установленная Java 17 / JDK 17).
3. Скрипт сам скачает Gradle, скомпилирует мод и положит готовый файл
   liminalworld-1.20.1-1.0.0.jar прямо в эту же папку.
4. Перекиньте liminalworld-1.20.1-1.0.0.jar в вашу папку .minecraft/mods!

Способ Б (Без установки Java/Gradle на ПК — через GitHub):
1. Загрузите файлы из архива в любой репозиторий на GitHub.
2. Во вкладке "Actions" автоматически запустится сборка (.github/workflows/build.yml)
   и выдаст готовый liminalworld-1.0.0.jar.
