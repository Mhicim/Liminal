package com.megastrata.world.data;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.*;

public class ExplorationSavedData extends SavedData {
    private static final String DATA_NAME = "megastrata_exploration_data";

    private final Set<String> discoveredSectors = new HashSet<>();
    private final Map<String, BlockPos> resonanceBeacons = new HashMap<>();
    private final List<String> architecturalJournalNotes = new ArrayList<>();

    public static ExplorationSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(ExplorationSavedData::load, ExplorationSavedData::new, DATA_NAME);
    }

    public void discoverSector(String sectorKey, String description) {
        if (discoveredSectors.add(sectorKey)) {
            architecturalJournalNotes.add(sectorKey + ": " + description);
            setDirty();
        }
    }

    public void registerBeacon(String name, BlockPos pos) {
        resonanceBeacons.put(name, pos);
        setDirty();
    }

    public Map<String, BlockPos> getResonanceBeacons() { return resonanceBeacons; }
    public List<String> getJournalEntries() { return architecturalJournalNotes; }

    public static ExplorationSavedData load(CompoundTag tag) {
        ExplorationSavedData data = new ExplorationSavedData();
        ListTag notes = tag.getList("JournalNotes", Tag.TAG_STRING);
        for (int i = 0; i < notes.size(); i++) {
            data.architecturalJournalNotes.add(notes.getString(i));
        }
        CompoundTag beacons = tag.getCompound("Beacons");
        for (String key : beacons.getAllKeys()) {
            data.resonanceBeacons.put(key, BlockPos.of(beacons.getLong(key)));
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag notes = new ListTag();
        for (String note : architecturalJournalNotes) {
            notes.add(StringTag.valueOf(note));
        }
        tag.put("JournalNotes", notes);

        CompoundTag beacons = new CompoundTag();
        resonanceBeacons.forEach((k, v) -> beacons.putLong(k, v.asLong()));
        tag.put("Beacons", beacons);
        return tag;
    }
}
