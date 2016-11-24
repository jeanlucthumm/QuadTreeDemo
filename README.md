# QuadTreeDemo

This program is a demonstration of a quad tree and a visualisation of its subdivision algorithm. Users can add points and watch the boundaries of each node in real time. A rectangle selection mechanism allows testing of the tree's ability to rapidly find points in a given region.

## Usage
**Primary Mouse Button**: Controls selection rectangle. All points in the region selected will be found and highlighted by the quad tree. Highlights reset every time a new selection is made.

**Secondary Mouse Button**: Adds a new point under the cursor.

![IMG](http://i.imgur.com/1WQUUEV.png)

### Compiling and Running
Navigate to the project directory. First you must compile the java classes:
`javac spaceindexer/*.java`

Then simply run the program:
`java spaceindexer.Main`

If you would like to clean up the compiled class files:
`rm -f spaceindexer/*.class`
