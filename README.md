# axon-avro
Collection of extensions and supporting components to use for Axon Avro serialization stack.

[![incubating](https://img.shields.io/badge/lifecycle-INCUBATING-orange.svg)](https://github.com/holisticon#open-source-lifecycle)
[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.holixon.axon.avro/axon-avro/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.holixon.axon.avro/axon-avro)
[![Build Status](https://github.com/holixon/axon-avro/workflows/Development%20branches/badge.svg)](https://github.com/holixon/axon-avro/actions)
[![Kotlin](https://img.shields.io/badge/kotlin-2.0.0-blue.svg?logo=kotlin)](http://kotlinlang.org)
[![Avro spec](https://img.shields.io/badge/avro%20spec-1.12.0-blue.svg?logo=apache)](https://avro.apache.org/docs/1.12.0/specification/)
[![sponsored](https://img.shields.io/badge/sponsoredBy-Holisticon-RED.svg)](https://holisticon.de/)

## Introduction

Axon Framework 4.11 introduced support for Avro Serialization of messages. This repository provides 
additions not included into Axon Framework but helpful in operations.

## Modules

Several modules are available:

* Axon Kotlin Serializer
* Axon Avro Server Plugin
* Generators: for Kotlin code generation around Avro

## How to use

Use our BOM:
```xml
<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>io.holixon.axon.avro</groupId>
      <artifactId>axon-avro-bom</artifactId>
      <packaging>pom</packaging>
      <scope>import</scope>
    </dependency>
  </dependencies>
</dependencyManagement>
```


Interested?
