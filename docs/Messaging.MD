Different Sections of the Data represent different hosts in the system. Section consists of one or more bocks.

When data become available on some host, actor responsible for its computation sends message with the topic 
`data-ready-%position.X-%position.Y`:
```$json
{
    "fileName": String;
    "sourceSectionId": Integer;
    "position": Position;
    "matrixType": MatrixType;
  
}
```
Other sections subscribed to the message would be able to download and store file locally.

When that happens, message with the topic `section-data-available-%matrixType-%position.X-%position.Y` is sent:
```$json
{
  "matrixType": String
  "filePath": String
  "sectionId": Integer
  "position": Position
}
``` 
Actors subscribed to that message can start loading data from the file to the memory and make computations. In case if 
the actor corresponding to position (position.X, position.Y) from the message received the message, it can just trigger 
computations because data is already in the memory.