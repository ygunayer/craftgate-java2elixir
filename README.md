# craftgate-java2elixir
Highly specific Java to Elixir converter that can be used to save time when syncing the [ygunayer/craftgate-elixir-client](https://github.com/ygunayer/craftgate-elixir-client) codebase with [craftgate/craftgate-java-client](https://github.com/craftgate/craftgate-java-client)

## How it Works
This project traverses the class hierarchy in `craftgate-java-client`, and for each data class and enumeration, it runs a conversion process and outputs them into the `output/craftgate` folder, with a sub path that matches the package path of the class, but converted into the Elixir conventions.

Enums make use of the `Craftgate.Enum.__using__/1` macro:

<table>
<thead>
    <tr>
        <th>Currency.java</th>
        <th>currency.ex</th>
    </tr>
</thead>
<tbody>
<tr>
<td>

```java
package io.craftgate.model;

public enum Currency {
    TRY,
    USD,
    EUR,
    GBP,
    ARS,
    BRL,
    CNY,
    AED,
    IQD
}
```

</td>
<td>

```elixir
defmodule Craftgate.Model.Currency do
  use Craftgate.Enum, [
    :AED,
    :ARS,
    :EUR,
    :GBP,
    :USD,
    :TRY,
    :BRL,
    :IQD,
    :CNY
  ]
end
```

</td>
</tr>
</tbody>
</table>

And data classes make use of the `Craftgate.Serializable.__using__/1` macro:

<table>
<thead>
    <tr>
        <th>FraudCheckParameters.java</th>
        <th>fraud_check_parameters.ex</th>
    </tr>
</thead>
<tbody>
<tr>
<td>

```java
package io.craftgate.request.dto;

import lombok.Data;

@Data
public class FraudCheckParameters {
    private String buyerExternalId;
    private String buyerPhoneNumber;
    private String buyerEmail;
}
```

</td>
<td>

```elixir
defmodule Craftgate.Request.Dto.FraudCheckParameters do
  use Craftgate.Serializable, [
    buyer_external_id: :string,
    buyer_phone_number: :string,
    buyer_email: :string
  ]
end
```

</td>
</tr>
</tbody>
</table>


## Usage
- Open up [build.gradle](./build.gradle) and change the Craftgate Java client version to the version you would like to use
- Run the `Application` class
- Copy the contents of the `output/craftgate` folder into the `lib/craftgate` folder of the Craftgate Elixir client 

# License
MIT
