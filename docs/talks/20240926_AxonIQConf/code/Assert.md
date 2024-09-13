```kotlin [11-12]
package io.holixon.axon.testing.examples.upcaster.junit5.kotlin  
  
class AccountCreatedEventUpcastingKotlinTest {
    
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
