package io.holixon.axon.avro.generation


object AxonAvroGeneration {

  object jmolecules {

    val ddd = try {
      Class.forName("org.jmolecules.ddd.annotation.AggregateRoot")
      true
    } catch (e: ClassNotFoundException) {
      false
    }

    val cqrs = try {
      Class.forName("org.jmolecules.ddd.annotation.AggregateRoot")
      true
    } catch (e: ClassNotFoundException) {
      false
    }
  }

}
