# craftgate-java2elixir
Highly specific Java to Elixir converter that can be used to save time when syncing the [ygunayer/craftgate-elixir-client](https://github.com/ygunayer/craftgate-elixir-client) codebase with [craftgate/craftgate-java-client](https://github.com/craftgate/craftgate-java-client)

Note that this project only converts data classes and enumerations, and does not support assigning default values to fields of a class.

## Usage
- Open up [build.gradle](./build.gradle) and change the Craftgate Java client version to the version you would like to use
- Run the `Application` class
- Copy the contents of the `output/craftgate` folder into the `lib/craftgate` folder of the Craftgate Elixir client 

# License
MIT
