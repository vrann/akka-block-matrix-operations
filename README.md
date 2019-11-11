# Block Matrix Manager with Akka

These are set of tools to implement distributed block matrix operations with the actor model.

**Distributed matrix operations**, such as matrix multiplication, matrix factorization, etc are operations which can be 
calculated concurrently on multiple computers

**Block matrix** is a way to represent large matrices as a combination of smaller size matrices. 
The size of the block, smaller matrix which original matrix consists of, is selected in the way that the operation on it 
can be efficiently calculated on one computer.

**Block Matrix algorithms** is the class of algorithms which express the calculation of the operation on original large matrix via 
the number of calculations on smaller matrices and the way to combine the result.

**Actor Model** is a conceptual model which describes the distributed computation as a complex choreography of the smaller
components which play the role in overall process. It is distinguished from other forms of the parallel/concurrent programing 
models by the way of expressing the process in declarative style. Usually what is declared are events happening in the process
and reactive handlers to those events.

**Akka** is the most popular implementation of the Actor Model approach to distributed computations.
It has state of the art tools and techniques built in which allows fast experimentations with the
approach

## Library for Actor Model application to Block Matrix Algorithms

This library provides high-level building blocks to implement Block Matrix Algorithms with the Akka.

### Topology

Distributed algorithm is executed on the set nodes -- 1..N machines
When matrix is splitted in smaller sub-matrices, blocks, every block is get assigned to the particular section.
Every node would be responsible for 1..M matrix blocks idenififed by it's coorinates in the matrix.
Logical representation of the nodes are **Sections** -- mapping of the matrix blocks to the execution nodes. 
Mapping is done via configuration file placed on the node.

When expressing block matrix algorithms in actor model, natural way to handle that is to have separate actor for each
block. However not all blocks would be processed differently.

Nature of the distributed block matrix algorithm is that different operations are applied to the different block types.
I.e. diagonal blocks vs sub-diagonal blocks, blocks in the first row/column, etc. That's why different block types would
be subscribed to the different events and execute different handlers. That's why there would be generic actor responsible 
for processing matrix block which would have custom logic depending on the position of the block in the matrix.

Section is subscribed to all the events which corresponds to every block it is consists of and executes handlers to 
those events.

### Componenets

**File Transfer component** sets up initial actor placement, basic events and coordination between actors to enable files
exchange. File transfer is initiated by the event about file is ready on 

**Section Coordinator** is responsible for 

**Generic Block Matrix Actor**

**Elements**: Based on position of the blocks there are different events and responsibilities. These differences are 
encapsulated in Elements logic. Currently supported elements are: Diagonal position (N, N), Sub-Diagonal position (N, M: M>N), 
Zero position (0,0)

**Handlers** are algorithm-specific pieces of logic attached to the elements to handle the events

## Algorithms implemented

Cholesky block matrix decomposition

## Deployment

The orchetsration of the system is done via kubernetes. When specifying new orchestration, kubernetes configuration should 
be changed to reflect