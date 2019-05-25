# Google PubSub testcontainer

:warning: alpha release, no guarantees

## Installation

```bash
git clone git@github.com:alde/pubsub-testcontainer.git
cd pubsub-testcontainer
gradle publishToMavenLocal
```
  
## Usage

See tests for more extensive usage

```java
  // Add classrule in your test class
class TestClass {
  private List<PubsubMessage> messageStore = new ArrayList<>();
  
  @ClassRule
  public static final GooglePubSubContainer<?> pubSubContainer =
      new GooglePubSubContainer<>();

  @BeforeClass
  public static void setupPubsub() {
      // Set up topic and subscription
      topic = ProjectTopicName.of(PROJECT, "topic-name");
          pubSubContainer.createTopic(topic);
    
      // Set up subscription and message store (used to verify published messages)
      ProjectSubscriptionName subscriptionName = ProjectSubscriptionName
        .of(PROJECT, "subscription-name");
      pubSubContainer.createSubscription(topic, subscriptionName);
      pubSubContainer.subscribe(subscriptionName, messageStore);
  }
  
  @Test
  public void publishMessage() {
      pubSubContainer
        .getPublisher()
        .publish(MESSAGE_BEING_PUBLISHED_FOR_OTHER_CONSUMERS);
      
      await().atMost(5, SECONDS).pollDelay(100, MILLISECONDS).untilAsserted(() -> {
          assertThat(messagestore)
            .hasSize(1);
          assertThat(messageStore.get(0).getData().toStringUtf8())
            .isEqualTo("the expected message");
      });
  }
}

```
