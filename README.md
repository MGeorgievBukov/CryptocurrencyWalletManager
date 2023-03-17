# Overview
CryptocurrencyWalletManager is a console application that allows users to store and manage their cryptocurrencies. It provides basic functions such as depositing, withdrawing, buying and selling crypto, checking wallet information, and getting information about crypto prices. The app also provides real-time information on the prices of different cryptocurrencies via the CoinAPI.

# Functionalities
## help
Displays information about the available commands and their parameters.

## register {name} {password}
Registers a new user in the database. If it already exists it does nothing.

## login {name} {password}
Logins with existing account credentials. If account is already used it does nothing.

## list-cryptos
Lists real time prices for the available cryptos.

## deposit {amount}
Deposits the given amount in the account.

## buy-crypto {id} {amount}
Buys cryptos with funds from the wallet.

## sell-crypto {id}
Sell crypto for funds in the wallet.

## wallet-information
Shows relevant information about the currently owned funds and cryptocurrencies.

## wallet-investment-information
Shows ROI from the owned cryptocurrencies.

## disconnect
Logs out from the account.

## shutdown
Shuts down the server
