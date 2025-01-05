package dev.alexanderdiaz.variorum.map;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

import java.util.List;

@Data
@Builder
public class VariorumMap {
    private String name;
    private List<String> authors;
    private List<Team> teams;
    private Spawns spawns;
    private Objectives objectives;

    @Data
    @Builder
    public static class Team {
        private String id;
        private String color;
        private String name;
    }

    @Data
    @Builder
    public static class Spawns {
        private SpawnRegion defaultSpawn;
        private List<TeamSpawn> teamSpawns;

        @Data
        @Builder
        public static class SpawnRegion {
            private Point point;
            private double yaw;
        }

        @Data
        @Builder
        public static class TeamSpawn {
            private String team;
            private SpawnRegion region;
        }
    }

    @Data
    @Builder
    public static class Point {
        private double x;
        private double y;
        private double z;

        public static Point fromString(String coord) {
            String[] parts = coord.split(",");
            return Point.builder()
                    .x(Double.parseDouble(parts[0].trim()))
                    .y(Double.parseDouble(parts[1].trim()))
                    .z(Double.parseDouble(parts[2].trim()))
                    .build();
        }

        @Override
        public String toString() {
            return String.format("%.1f,%.1f,%.1f", x, y, z);
        }
    }

    @Data
    @Builder
    public static class Objectives {
        private Monuments monuments;

        @Data
        @Builder
        public static class Monuments {
            private String materials;
            private List<TeamMonuments> teamMonuments;

            @Data
            @Builder
            public static class TeamMonuments {
                private String owner;
                private List<Monument> monuments;

                @Data
                @Builder
                public static class Monument {
                    private String name;
                    private Region region;

                    @Data
                    @Builder
                    public static class Region {
                        private Point block;
                    }
                }
            }
        }
    }
}