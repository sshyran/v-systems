package scorex.database

import org.scalatest.prop.{GeneratorDrivenPropertyChecks, PropertyChecks}
import org.scalatest.{Matchers, PropSpec}
import scorex.transaction.ValidationError

class EntrySpecification extends PropSpec
with PropertyChecks
with GeneratorDrivenPropertyChecks
with Matchers {

  property("convert entry to byte and convert back") {
    val name = "key1"
    val data = "value1"
    val entry = Entry.buildEntry(data, name, DataType.ByteArray)
    entry.map(_.bytes).map(_.arr).flatMap(Entry.fromBytes(_)) should be (entry)
  }

  property("report invalid data type") {
    val byteArray1 = Array(0, 4, 107, 101, 121, 49, 0, 118, 97, 108, 117, 101, 49).map(_.asInstanceOf[Byte])
    val byteArray2 = Array(0, 4, 107, 101, 121, 49, 3, 118, 97, 108, 117, 101, 49).map(_.asInstanceOf[Byte])
    val byteArray3 = Array(0, 4, 107, 101, 121, 49, 1, 118, 97, 108, 117, 101, 49).map(_.asInstanceOf[Byte])
    Entry.fromBytes(byteArray1) should be (Left(ValidationError.InvalidDataType))
    Entry.fromBytes(byteArray2) should be (Left(ValidationError.InvalidDataType))
    Entry.fromBytes(byteArray3).map(_.dataType) should be (Right(DataType.ByteArray))
  }
}