package geopyspark.geotrellis.protobufs

import geopyspark.geotrellis.ProtoBufCodec
import geopyspark.geotrellis.protobufs.Implicits._
import protos.extentMessages._
import protos.tileMessages._
import protos.keyMessages._
import protos.tupleMessages._

import geotrellis.raster._
import geotrellis.vector._
import geotrellis.spark._


trait TupleProtoBuf {

  implicit def tupleProjectedExtentProtoBufCodec =
    new ProtoBufCodec[(ProjectedExtent, MultibandTile), ProtoTuple] {
    def encode(tuple: (ProjectedExtent, MultibandTile)): ProtoTuple =
      ProtoTuple(
        projectedExtent = Some(projectedExtentProtoBufCodec.encode(tuple._1)),
        tiles = Some(multibandTileProtoBufCodec.encode(tuple._2)))

    def decode(message: ProtoTuple): (ProjectedExtent, MultibandTile) =
      (projectedExtentProtoBufCodec.decode(message.projectedExtent.get),
        multibandTileProtoBufCodec.decode(message.tiles.get))
  }

  implicit def tupleTemporalProjectedExtentProtoBufCodec =
    new ProtoBufCodec[(TemporalProjectedExtent, MultibandTile), ProtoTuple] {
    def encode(tuple: (TemporalProjectedExtent, MultibandTile)): ProtoTuple =
      ProtoTuple(
        temporalProjectedExtent = Some(temporalProjectedExtentProtoBufCodec.encode(tuple._1)),
        tiles = Some(multibandTileProtoBufCodec.encode(tuple._2)))

    def decode(message: ProtoTuple): (TemporalProjectedExtent, MultibandTile) =
      (temporalProjectedExtentProtoBufCodec.decode(message.temporalProjectedExtent.get),
        multibandTileProtoBufCodec.decode(message.tiles.get))
  }

  implicit def tupleSpatialKeyProtoBufCodec =
    new ProtoBufCodec[(SpatialKey, MultibandTile), ProtoTuple] {
    def encode(tuple: (SpatialKey, MultibandTile)): ProtoTuple =
      ProtoTuple(
        spatialKey = Some(spatialKeyProtoBufCodec.encode(tuple._1)),
        tiles = Some(multibandTileProtoBufCodec.encode(tuple._2)))

    def decode(message: ProtoTuple): (SpatialKey, MultibandTile) =
      (spatialKeyProtoBufCodec.decode(message.spatialKey.get),
        multibandTileProtoBufCodec.decode(message.tiles.get))
  }

  implicit def tupleSpaceTimeKeyProtoBufCodec =
    new ProtoBufCodec[(SpaceTimeKey, MultibandTile), ProtoTuple] {
    def encode(tuple: (SpaceTimeKey, MultibandTile)): ProtoTuple =
      ProtoTuple(
        spaceTimeKey = Some(spaceTimeKeyProtoBufCodec.encode(tuple._1)),
        tiles = Some(multibandTileProtoBufCodec.encode(tuple._2)))

    def decode(message: ProtoTuple): (SpaceTimeKey, MultibandTile) =
      (spaceTimeKeyProtoBufCodec.decode(message.spaceTimeKey.get),
        multibandTileProtoBufCodec.decode(message.tiles.get))
  }
}
