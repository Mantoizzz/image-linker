package entity;

public record TileHash(long hashR, long hashG, long hashB) {

    public int hammingDistance(TileHash other) {
        return Long.bitCount(this.hashR ^ other.hashR)
                + Long.bitCount(this.hashG ^ other.hashG)
                + Long.bitCount(this.hashB ^ other.hashB);
    }
}
