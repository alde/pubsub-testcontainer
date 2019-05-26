# Google PubSub testcontainer
[![CircleCI](https://circleci.com/gh/alde/pubsub-testcontainer/tree/master.svg?style=svg)](https://circleci.com/gh/alde/pubsub-testcontainer/tree/master)
![Maven Central](https://img.shields.io/maven-central/v/nu.alde/pubsub-testcontainer.svg?style=flat-square)

## Installation

```xml
<dependency>
    <groupId>nu.alde</groupId>
    <artifactId>pubsub-testcontainer</artifactId>
    <version>0.0.5</version>
</dependency>
```

## Issues

Transitive dependencies of google-cloud-pubsub are messy
  
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
