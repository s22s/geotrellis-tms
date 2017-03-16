package geopyspark.geotrellis.spark

import geopyspark.geotrellis._
import geopyspark.geotrellis.GeoTrellisUtils._

import geotrellis.util._
import geotrellis.proj4._
import geotrellis.raster._
import geotrellis.vector._
import geotrellis.spark._
import geotrellis.spark.io._
import geotrellis.spark.io.json._
import geotrellis.spark.io.avro._
import geotrellis.spark.tiling._

import spray.json._

import org.apache.spark._
import org.apache.spark.rdd.RDD

import scala.collection.JavaConverters._
import collection.JavaConversions._
import java.util.Map

import scala.reflect.ClassTag


object TileLayerMetadataCollector {
  private def createCollection[
  K: GetComponent[?, ProjectedExtent]: (? => TilerKeyMethods[K, K2]): AvroRecordCodec: ClassTag,
  T <: CellGrid: AvroRecordCodec: ClassTag,
  K2: Boundable: SpatialComponent: JsonFormat
  ](
    returnedRdd: RDD[Array[Byte]],
    schemaJson: String,
    pythonExtent: java.util.Map[String, Double],
    pythonTileLayout: java.util.Map[String, Int],
    crsJavaMap: java.util.Map[String, String]
  ): String = {

    val rdd =
      PythonTranslator.fromPython[(K, T)](returnedRdd, Some(schemaJson))

    val layoutDefinition = LayoutDefinition(pythonExtent.toExtent,
      pythonTileLayout.toTileLayout)

    val crs: Option[CRS] = crsJavaMap.toCrs

    val metadata =
      crs match {
        case None => rdd.collectMetadata[K2](layoutDefinition)
        case Some(x) => rdd.collectMetadata[K2](x, layoutDefinition)
      }

    metadata.toJson.compactPrint
  }

  def collectPythonMetadata(
    keyType: String,
    returnedRdd: RDD[Array[Byte]],
    schemaJson: String,
    pythonExtent: java.util.Map[String, Double],
    pythonTileLayout: java.util.Map[String, Int],
    crsJavaMap: java.util.Map[String, String]
  ): String =
    keyType match {
      case "ProjectedExtent" =>
        createCollection[ProjectedExtent, MultibandTile, SpatialKey](
          returnedRdd,
          schemaJson,
          pythonExtent,
          pythonTileLayout,
          crsJavaMap)
      case "TemporalProjectedExtent" =>
        createCollection[TemporalProjectedExtent, MultibandTile, SpaceTimeKey](
          returnedRdd,
          schemaJson,
          pythonExtent,
          pythonTileLayout,
          crsJavaMap)
    }

  private def createPyramidCollection[
    K: GetComponent[?, ProjectedExtent]: (? => TilerKeyMethods[K, K2]): AvroRecordCodec: ClassTag,
    T <: CellGrid: AvroRecordCodec: ClassTag,
    K2: Boundable: SpatialComponent: JsonFormat
  ](
    returnedRdd: RDD[Array[Byte]],
    schemaJson: String,
    crs: String,
    tileSize: Int,
    resolutionThreshold: Double,
    maxZoom: Int,
    crsJavaMap: java.util.Map[String, String]
  ): (Int, String) = {
    val rdd = PythonTranslator.fromPython[(K, T)](returnedRdd, Some(schemaJson))

    val zoomedLayout= ZoomedLayoutScheme(CRS.fromName(crs), tileSize, resolutionThreshold)
    val outputCRS: Option[CRS] = crsJavaMap.toCrs

    val (zoomLevel, metadata) =
      outputCRS match {
        case None => TileLayerMetadata.fromRdd(rdd, zoomedLayout, maxZoom)
        case Some(x) => TileLayerMetadata.fromRdd(rdd, x, zoomedLayout, maxZoom)
      }

    (zoomLevel, metadata.toJson.compactPrint)
  }

  def collectPythonPyramidMetadata(
    keyType: String,
    returnedRDD: RDD[Array[Byte]],
    schemaJson: String,
    crs: String,
    tileSize: Int,
    resolutionThreshold: Double,
    maxZoom: Int,
    crsJavaMap: java.util.Map[String, String]
  ): (Int, String) =
      keyType match {
        case "ProjectedExtent" =>
          createPyramidCollection[ProjectedExtent, MultibandTile, SpatialKey](
            returnedRDD,
            schemaJson,
            crs,
            tileSize,
            resolutionThreshold,
            maxZoom,
            crsJavaMap)
        case "TemporalProjectedExtent" =>
          createPyramidCollection[TemporalProjectedExtent, MultibandTile, SpaceTimeKey](
            returnedRDD,
            schemaJson,
            crs,
            tileSize,
            resolutionThreshold,
            maxZoom,
            crsJavaMap)
      }
}
