#!/bin/bash
pushd /tmp > /dev/null

openssl ecparam -name prime256v1 -genkey -noout -out vapid_private.pem 2> /dev/null
openssl ec -in vapid_private.pem -pubout -out vapid_public.pem 2> /dev/null

echo "Public key:"
echo
openssl ec -in vapid_private.pem -pubout -text -noout 2> /dev/null | grep "pub:" -A5 | sed 1d | xxd -r -p | base64 | paste -sd "\0" -
echo
echo "Private key:"
echo
openssl ec -in vapid_private.pem -pubout -text -noout 2> /dev/null | grep "priv:" -A3 | sed 1d | xxd -r -p | base64 | paste -sd "\0" -
echo

rm vapid_private.pem
rm vapid_public.pem

popd > /dev/null