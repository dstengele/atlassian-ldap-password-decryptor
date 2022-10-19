import javax.crypto.spec.SecretKeySpec
import groovy.json.JsonSlurper
import javax.crypto.SealedObject
import java.io.FileInputStream
import java.io.ObjectInputStream
import java.io.ByteArrayInputStream
import java.util.Base64
import groovy.cli.commons.CliBuilder
import groovy.cli.commons.OptionAccessor

// Read keydir cli argument
CliBuilder cli = new CliBuilder(usage: "${this.class.getName()} -k")
cli.with {
   k longOpt: 'keydir', args:1, argName: 'keydir', required: true, 'Directory containing encryption keys'
}

OptionAccessor options = cli.parse(args)

if (!options?.k) {
   System.exit(1)
}

String KEY_DIR = options.k // Usually SHARED_HOME/keys

JsonSlurper jsonReader = new JsonSlurper()

// Read DB data from stdin
String input = System.in.readLines().join("\n")

// Remove algorithm prefix from db data to get valid json
String json = input - ~/\{.*?\}/
Map keyDataFromDb = jsonReader.parseText(json)

// Read keyfile and deserialize into new object
String keyFilePath = keyDataFromDb.get("keyFilePath", "").replace("KEY_DIR", KEY_DIR)
ObjectInputStream ois = new ObjectInputStream(new FileInputStream(keyFilePath))
SecretKeySpec key = ois.readObject() as SecretKeySpec

// Decode base64 representation of sealedObject and deserialize into SealedObject
String sealedObjectString = keyDataFromDb.get("serializedSealedObject", "")
byte[] sealedObjectBytes = Base64.getDecoder().decode(sealedObjectString)
ObjectInputStream sealedObjectStream = new ObjectInputStream(new ByteArrayInputStream(sealedObjectBytes))
SealedObject sealedObject = sealedObjectStream.readObject() as SealedObject

// Decrypt SealedObject with key
String orig = sealedObject.getObject(key) as String

// Split password and salt
List<String> tokens = orig.tokenize("|")
String password = tokens.first()

// Print the password to stdout
println(password)
