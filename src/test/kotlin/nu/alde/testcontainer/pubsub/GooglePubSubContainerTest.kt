package nu.alde.testcontainer.pubsub

import com.google.protobuf.ByteString
import com.google.pubsub.v1.ProjectSubscriptionName
import com.google.pubsub.v1.ProjectTopicName
import com.google.pubsub.v1.PubsubMessage
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Awaitility.await
import org.junit.ClassRule
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.TimeUnit.SECONDS

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class GooglePubSubContainerTest {
    private val PROJECT_ID = "gcp-project"
    private val topic = ProjectTopicName.of(PROJECT_ID, "my-topic")
    private val subscription = ProjectSubscriptionName
        .of(PROJECT_ID, "my-subscription")
    private val messages = mutableListOf<PubsubMessage>()

    @ClassRule
    val pubsubContainer: GooglePubSubContainer<*> = GooglePubSubContainer<Nothing>().apply {
        start()
    }

    @BeforeAll
    internal fun setupTopicAndSubscription() {
        pubsubContainer.createTopic(topic)
        pubsubContainer.createSubscription(topic, subscription)
        pubsubContainer.subscribe(subscription, messages)
    }

    @Test
    fun startEmulator() {
        assertThat(pubsubContainer.isRunning())
            .isTrue()
        assertThat(pubsubContainer.getContainerIpAddress())
            .isEqualTo("localhost")
        assertThat(pubsubContainer.getMappedPort(8888))
            .isNotZero()
    }

    @Test
    fun createTopicIsCreated() {
        val publisher = pubsubContainer.getPublisher(topic)

        assertThat(publisher.topicNameString)
            .isEqualTo("projects/gcp-project/topics/my-topic")
    }

    @Test
    fun createSubscription() {
        val s = pubsubContainer.getSubscription(subscription)
        assertThat(s.name)
            .endsWith("my-subscription")
    }

    @Test
    fun publishAndConsumeMessage() {
        val messageContent = "A message sent on pubsub"
        val message = PubsubMessage.newBuilder()
            .setData(ByteString.copyFromUtf8(messageContent))
            .setMessageId("1")
            .build()

        pubsubContainer.getPublisher(topic)
            .publish(message)

        await().atMost(5, SECONDS).pollDelay(100, MILLISECONDS).untilAsserted {
            assertThat(messages)
                .hasSize(1)
            assertThat(messages[0].data.toStringUtf8())
                .isEqualTo(messageContent)
        }
    }
}