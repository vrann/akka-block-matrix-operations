package com.vrann.actormatrix.block;


import akka.cluster.pubsub.DistributedPubSubMediator;
import com.vrann.actormatrix.BlockMatrixAction;
import com.vrann.actormatrix.Message;
import com.vrann.actormatrix.Position;
import com.vrann.actormatrix.cholesky.message.A11MatrixDataAvailable;
import com.vrann.actormatrix.cholesky.message.L21MatrixDataAvailable;

import java.util.*;
import java.util.function.*;

/*

                                             ____________
                                            |            |
L21 (1, i), (2, i), .., (i-1, i)    ----->  | A11 (i, i) |
                                            |____________|
                                                  |
                                                  |
                                                  V
                                               L11 (i, i)
 */
/*enum DiagonalBlockState {
    ACTOR_STARTED,
    BLOCK_SUBSCRIBED,
    ANN_DATA_READY,
    A22_DATA_READY,
    A11_DATA_READY,
    L11_CALCULATED
}


/*

Sub-diagonal Blocks

                                                L11 (i, i)
                                                  |
                                                  |
                                             _____V______
                                            |           |
L21 (1, j), (2, j), .., (i-1, j)    ----->  | aMN (i,j) | ----> L21 (i,j)
                                            |___________|

A22 is not an external state, it is aMN with applied L21
We cannot apply L11 before all L21 received and applied, We can only receive it

 */


/*enum SubDiagonalBlockState {
    ACTOR_STARTED,
    BLOCK_SUBSCRIBED,
    AMN_DATA_READY,
    A22_DATA_READY,
    L21_CALCULATED
}

enum DiagonalBlockTransition {

    SUBSCRIBE(DiagonalBlockState.ACTOR_STARTED, DiagonalBlockState.BLOCK_SUBSCRIBED),
    INIT_DATA(DiagonalBlockState.BLOCK_SUBSCRIBED, DiagonalBlockState.ANN_DATA_READY),
    RECEIVE_A22(DiagonalBlockState.ANN_DATA_READY, DiagonalBlockState.A22_DATA_READY);

    private final DiagonalBlockState from;
    private final DiagonalBlockState to;

    DiagonalBlockTransition(DiagonalBlockState from, DiagonalBlockState to) {
        this.from = from;
        this.to = to;
    }
}

enum ExpectationType {
    SUBSCRIPTIONS,
    APPLY_L21,
    RECEIVE_L21
}
*/


/*

from_state -> to_state: [on_conditions]


subscription:
    ACTOR_STARTED -> BLOCK_SUBSCRIBED

data:
    ACTOR_STARTED -> DATA_READY

receive updates:
    ACTOR_STARTED -> L11_RECEIVED -> L11_APPLIED //not applicable to diagonal
    ACTOR_STARTED -> L21_RECEIVED -> L21_APPLIED

apply updates:
    DATA_READY -> L11_APPLIED
    DATA_READY -> L21_APPLIED

calculate
    DATA_READY, L11_APPLIED, L21_APPLIED -> L11_CALCULATED

ACTOR_STARTED -> BLOCK_SUBSCRIBED: RECEIVE_SUBSCRIPTIONS_ACKS // might not receive important messages before this happens but we still would receive whatever comes. Needed for debugging


blockSubscribed {
  boolean areAllReceived
  List<String> received
}

BLOCK_SUBSCRIBED -> DATA_READY: ANN_RECEIVED // can receive other files too, but before this received cannot do anything with them. So receive and register
so it's not really transition from one state to another, it is rather compound state

dataReady {
  boolean isReceived
  String received
}

DATA_READY -> L11_CALCULATED: CALCULATE_L11 // diagonal

l11Calculated {
  boolean isCalculated // is dataReady.isReceived && l21Ready.areAllReceived
}

DATA_READY -> L11_RECEIVED: RECEIVE_L11 //subdiagonal
l11Received {
    boolean isReceived
}

RECEIVE_L11 -> L11_APPLIED: APPLY_L11 //subdiagonal
l11Applied implements SingleCondition {
    boolean isApplied
}

DATA_READY -> L21_RECEIVED: RECEIVE_L21
l21Received implements Condition, MultipleStates {
    boolean areAllReceived
    List<Position> received
}

l21Applied implements MultiCondition {
    boolean areAllApplied
    List<Position> applied
}

Condition {

    boolean doesSatisfy

    toString() {
        //compile debug
    }
}

MultiCondition extends Conition {
    default toString() {

    }

    default doesSatisfy
}

SingleCondition extends Conition {
    default toString() {

    }

    default doesSatisfy
}

MultiCondition: [SingleCondition, SingleCondition(Pos(0, 2)), SingleCondition(Pos(0, 1))]

start -> do shit -> end
start -> subscribe -> receive data -> receive l21 -> calculate l11 -> done
start -> subscribe -> receive data -> receive l11 -> apply l11 -> receive l21 -> apply l21 -> done
some of the steps depends on others but rest can be in any order

Condition.addPreCondition(Condition preCondition)
Condition.sendSignal(T message)

Map<Condition> conditions
conditions registerForSignal()
conditions.get().sendSignal(message)

condition.onSatisfy(Callback c)

StateMachine.isCompleted
stateMachine.whatsNext()

StateMachine {
    assert(Condition preCondition)
}

we can receive both L11 and L21 for subdiagonal block being in any state
we can apply them only if DATA_READY

we can receive L21 for diagonal block being in any state, we can calculate L11 only is all L21 received, applied  and data ready

 */


/*interface Condition {

    boolean satisfied();

    void addPreCondition(Condition preCondition);

    //void subscribe(UnaryOperator<> callback);
}

interface MultiCondition extends Condition {

    void addCondition(Condition condition);
}

class SubscribedTopicCondition implements Condition {

    private boolean satisfied = false;
    private final List<Condition> preConditions = new ArrayList<>();
    private String topic;

    public SubscribedTopicCondition(String topic) {
        this.topic = topic;
    }

    public boolean satisfied() {
        for (Condition preCondition: preConditions) {
            if (!preCondition.satisfied()) {
                return false;
            }
        }
        return satisfied;
    }

    public void addPreCondition(Condition preCondition) {
        //preConditions.sub
        preConditions.add(preCondition);
    }

    public void inform() {

    }

    public void subscribe(UnaryOperator<Condition> callback) {

    }
}

class SubscribedCondition implements MultiCondition {

    private final List<Condition> conditions = new ArrayList<>();
    private final List<Condition> preConditions = new ArrayList<>();

    public void addPreCondition(Condition condition) {
        preConditions.add(condition);
    }

    public void addCondition(Condition condition) {
        conditions.add(condition);
    }

    public boolean satisfied() {
        for (Condition preCondition: preConditions) {
            if (!preCondition.satisfied()) {
                return false;
            }
        }

        for (Condition condition: conditions) {
            if (!condition.satisfied()) {
                return false;
            }
        }

        return true;
    }

    UnaryOperator<Condition> callWhenSatisfied = (condition) -> {
        System.out.println(condition);
        return null;
    };
}

class TopicProducer {

    public String produce() {
        return "topic1";
    }

    public void subscribe(String topic, UnaryOperator<String> callback) {

    }
}

class InitializeConditions {

    public static void initialize() {
        List<String> topics = List.of("topic1", "topic2", "topic3");
        SubscribedCondition subscribedCondition = new SubscribedCondition();
        TopicProducer producer = new TopicProducer();
        for (String topic: topics) {

            SubscribedTopicCondition topicSubscribedCondition = new SubscribedTopicCondition(topic);
            producer.subscribe(topicSubscribedCondition.inform);

            topicSubscribedCondition.subscribe(subscribedCondition.callWhenSatisfied);
            subscribedCondition.addCondition(topicSubscribedCondition);
        }

        producer.produce("topic1");
        producer.produce("topic1");
        producer.produce("topic1");

        A11MatrixDataProducer a11DataProducer = new A11DataProducer();
        a11DataProducer.produce(A11MatrixDataAvailable message);
    }
}



enum  BlockState {
    INIT,
    COMPLETE
}

enum HandlerState {
    INIT,
    COMPLETE
}

class StateEventHandler {

    private final List<Consumer<HandlerState>> completionListeners = new ArrayList<>();

    public void onComplete(Consumer<HandlerState> consumer) {
        completionListeners.add(consumer);
    }

    private void complete() {
        for (Consumer listener: completionListeners) {
            listener.accept(HandlerState.COMPLETE);
        }
    }

    public void triggerEvent(Message message) {

    }
}

class BlockActor {

    private final StateManagement stateManagement;

    public BlockActor(StateManagement stateManagement) {
        this.stateManagement = stateManagement;
    }

    void handleMessage(Message message) {
        stateManagement.triggerEvent(StateEvent.RECEIVED, message);
        //process
        stateManagement.notify(StateEvent.PROCESSED, message);

    }
}

enum StateEvent {
    RECEIVED,
    PROCESSED
}




public class BlockStateMachine {

    private final Position position;
    //private final Expectation[] expectations;
    private DiagonalBlockState state;

    private Map<ExpectationType, Expectation> expectedChecks;

    public static BlockStateMachine createDiagonal(Position position) {
        Expectation[] expectations = new Expectation[]{
            new ExpectReceiveL21(position),
            new ExpectApplyL21(position),
            new ExpectSubscriptions()
        };

        Map<ExpectationType, Expectation> expectedChecks = new HashMap<>();
        expectedChecks.put(ExpectationType.SUBSCRIPTIONS, new ExpectSubscriptions());
        expectedChecks.put(ExpectationType.APPLY_L21, new ExpectApplyL21(position));
        expectedChecks.put(ExpectationType.RECEIVE_L21, new ExpectReceiveL21(position));
        return new BlockStateMachine(position, expectedChecks);
    }

    public static BlockStateMachine createSubDiagonal(Position position) {
        Map<ExpectationType, Expectation> expectedChecks = new HashMap<>();
        expectedChecks.put(ExpectationType.SUBSCRIPTIONS, new ExpectSubscriptions());
        expectedChecks.put(ExpectationType.APPLY_L21, new ExpectApplyL21(position));
        expectedChecks.put(ExpectationType.RECEIVE_L21, new ExpectReceiveL21(position));
        return new BlockStateMachine(position, expectedChecks);
    }

    public Expectation getExpectation(ExpectationType type) {
        return expectedChecks.get(type);
    }

    public BlockStateMachine(Position position, Map<ExpectationType, Expectation> expectedChecks) {
        this.position = position;
        this.expectedChecks = expectedChecks;
        this.state = DiagonalBlockState.ACTOR_STARTED;
    }

    public void receive(Object message) {

        expectations.match(message).receive(message);

        switch (state) {
            case ACTOR_STARTED:
                if (getExpectation(ExpectationType.SUBSCRIPTIONS).areAllReceived()) {
                    state = DiagonalBlockState.BLOCK_SUBSCRIBED;
                }
                break;
            case BLOCK_SUBSCRIBED:
                if (getExpectation(ExpectationType.RECEIVE_L21).areAllReceived()) {
                    state = DiagonalBlockState.ANN_DATA_READY;
                }
                break;
        }
    }

    public boolean areAllReceived() {
        for (Expectation e: expectations) {
            e.areAllReceived();
        }
        return false;
    }

    public void inform(BlockMatrixAction action, A11MatrixDataAvailable message) {
        switch (action) {
            case RECEIVE:
                //do this
                break;
            case PROCESS:
                //do that
                break;
        }
    }
}

class ExpectReceiveL21 implements Expectation<L21MatrixDataAvailable> {
    private final Set<Position> expectedL21 = new HashSet<>();
    private final Set<Position> receivedL21 = new HashSet<>();
    private final Position position;

    public ExpectReceiveL21(Position position) {
        this.position = position;
        initExpectations();
    }

    @Override
    public void receive(L21MatrixDataAvailable message) {
        if (expectedL21.contains(message.getPosition())) {
            receivedL21.add(message.getPosition());
            expectedL21.remove(message.getPosition());
        }
    }

    @Override
    public boolean areAllReceived() {
        return expectedL21.size() == 0;
    }

    private void initExpectations() {
        for (int i = 0; i < position.getY(); i++) {
            expectedL21.add(Position.fromCoordinates(position.getX(), i));
        }
    }
}

class ExpectApplyL21 implements Expectation<L21MatrixDataAvailable> {
    private final Set<Position> expectedA22 = new HashSet<>();
    private final Set<Position> calculatedA22 = new HashSet<>();
    private final Position position;

    public ExpectApplyL21(Position position) {
        this.position = position;
        initExpectations();
    }

    @Override
    public void receive(L21MatrixDataAvailable message) {
        if (expectedA22.contains(message.getPosition())) {
            calculatedA22.add(message.getPosition());
            expectedA22.remove(message.getPosition());
        }
    }

    @Override
    public boolean areAllReceived() {
        return expectedA22.size() == 0;
    }

    private void initExpectations() {
        for (int i = 0; i < position.getY(); i++) {
            expectedA22.add(Position.fromCoordinates(position.getX(), i));
        }
    }
}

class ExpectSubscriptions implements Expectation<DistributedPubSubMediator.SubscribeAck> {

    private final Set<Position> expectedTopics = new HashSet<>();
    private final Set<Position> receivedTopics = new HashSet<>();

    @Override
    public void receive(DistributedPubSubMediator.SubscribeAck message) {
        message.subscribe().topic();
    }

    @Override
    public boolean areAllReceived() {
        return false;
    }

    private void initExpectations() {

    }
}*/