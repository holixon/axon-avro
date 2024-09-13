```kotlin [3-5,7-13,15-20]
class AccountCreatedEventUpcastingKotlinTest {

	companion object {  
	  val jacksonSerializer = JacksonSerializer.builder().lenientDeserialization().objectMapper(jacksonObjectMapper()).build()
	}
	  
    val jsonUpcaster = jsonNodeUpcaster(AccountCreatedEvent::class.java.name, "12", "13") {  
 	  (it as ObjectNode).apply {  
	    put("accountId", get("bankAccountId").asText())  
	    remove("bankAccountId")  
	    put("maximalBalance", 1000)  
	  }  
	}
	
	@UpcasterTest(messageEncoding = MessageEncoding.JACKSON)  
	fun `upcasts account created jackson`(
	  events: List<IntermediateEventRepresentation>, 
	  result: List<IntermediateEventRepresentation>
	) {    
	  val upcastedStream = jsonUpcaster.upcast(events.stream())  
	  AxonAssertions.assertThat(upcastedStream, jacksonSerializer)  
	    .containsExactlyDeserializedElementsOf(result.stream(), AccountCreatedEvent::class.java)  
	}
}

```
