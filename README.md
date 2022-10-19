# Atlassian LDAP Password Decryptor

Since Jira 8.14, the LDAP password for User Directories is "encrypted" with a
key that is saved in the (shared) home directory.

As it can be useful to get the saved password e.g. for debugging with
`ldapsearch`, this script takes the encrypted entry from the database on
standard input and returns the decrypted password.

## Usage
```bash
psql -At -c "select attribute_value from cwd_directory_attribute where attribute_name = 'ldap.password' and directory_id = 10000;" | groovy decrypt.groovy -k /var/opt/jira-shared-home/keys`
```
