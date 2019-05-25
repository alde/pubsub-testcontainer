package nu.alde.testcontainer.pubsub

import com.github.dockerjava.api.command.InspectContainerResponse
import com.google.api.gax.core.NoCredentialsProvider
import com.google.api.gax.grpc.GrpcTransportChannel
import com.google.api.gax.rpc.FixedTransportChannelProvider
import com.google.cloud.pubsub.v1.*
import com.google.pubsub.v1.*
import io.grpc.ManagedChannelBuilder
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.LogMessageWaitStrategy
import java.io.IOException
import java.util.*

class GooglePubSubContainer<SelfT : GooglePubSubContainer<SelfT>> @JvmOverloads constructor(imageName: String = "google/cloud-sdk:latest") :
    GenericContainer<SelfT>(imageName) {
    private var channelProvider: FixedTransportChannelProvider? = null
    private val credentialsProvider = NoCredentialsProvider.create()

    private val transportChannelProvider: FixedTransportChannelProvider
        get() {
            val channel = ManagedChannelBuilder.forAddress(
                containerIpAddress,
                getMappedPort(PUBSUB_PORT)
            ).usePlaintext().build()

            return FixedTransportChannelProvider.create(
                GrpcTransportChannel.create(channel)
            )
        }

    override fun configure() {
        startupAttempts = 3
        withExposedPorts(PUBSUB_PORT)
        withCommand(
            "/bin/sh",
            "-c",
            "gcloud beta emulators pubsub start --project testing --host-port=0.0.0.0:8888"
        )
        waitingFor(
            LogMessageWaitStrategy().withRegEx("(?s).*started.*$")
        )
    }

    /**
     *
     */
    override fun containerIsStarted(containerInfo: InspectContainerResponse) {
        channelProvider = transportChannelProvider
    }

    /**
     * Create a topic in the pubsub emulator.
     *
     * @param topic
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createTopic(topic: ProjectTopicName) {
        val topicAdminClient = TopicAdminClient.create(
            TopicAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build()
        )
        topicAdminClient.createTopic(topic)
    }

    /**
     * Create a subscription on the given topic in the pubsub emulator
     *
     * @param topicName name of the topic
     * @param name name of the subscription
     * @throws IOException
     */
    @Throws(IOException::class)
    fun createSubscription(
        topicName: ProjectTopicName,
        name: ProjectSubscriptionName
    ) {

        val subscriptionAdminClient = SubscriptionAdminClient.create(
            SubscriptionAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build()
        )

        subscriptionAdminClient
            .createSubscription(
                name,
                topicName,
                PushConfig.getDefaultInstance(), 0
            )
    }

    /**
     * Get the publisher that can be used to publish messages on the topic
     *
     * @param topic Topic to publish to
     */
    @Throws(IOException::class)
    fun getPublisher(topic: TopicName): Publisher {
        return Publisher.newBuilder(topic)
            .setChannelProvider(channelProvider)
            .setCredentialsProvider(credentialsProvider)
            .build()
    }

    /**
     * Get a subscription by name
     *
     * @param name Name of the subscription to get
     */
    fun getSubscription(name: ProjectSubscriptionName): Subscription {
        val subscriptionAdminClient = SubscriptionAdminClient.create(
            SubscriptionAdminSettings.newBuilder()
                .setTransportChannelProvider(channelProvider)
                .setCredentialsProvider(credentialsProvider)
                .build()
        )
        return subscriptionAdminClient.getSubscription(name)
    }

    /**
     * Add a listener to a subscription. Events consumed will be put on the messageStore.
     *
     * @param subscriptionName Name of the subscription to listen to
     * @param messageStore A mutable list where PubSub messages consumed will be stored
     */
    fun subscribe(
        subscriptionName: ProjectSubscriptionName,
        messageStore: MutableList<PubsubMessage>
    ) {
        val subscriber = Subscriber.newBuilder(subscriptionName) { pubsubMessage, ackReplyConsumer ->
            messageStore.add(pubsubMessage)
            ackReplyConsumer.ack()
        }.setChannelProvider(channelProvider).setCredentialsProvider(credentialsProvider).build()

        subscriber.startAsync()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        if (other == null || other !is GooglePubSubContainer<*>) {
            return false
        }
        if (!super.equals(other)) {
            return false
        }
        val that = other as GooglePubSubContainer<*>?
        return Objects.equals(channelProvider, that!!.channelProvider)
    }

    override fun hashCode(): Int {
        return Objects.hash(super.hashCode(), channelProvider)
    }

    companion object {
        private const val PUBSUB_PORT = 8888
    }
}
