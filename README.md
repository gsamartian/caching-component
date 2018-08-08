# Caching Component

## Purpose
* The purpose of this component is to encapsulate the caching implementation.
* Currently, it wraps the Redis Implementation.
* It uses the spring specific dependencies for caching.



## It also adds a few custom features as  below which are not available by default in Spring Caching
* Configuring Cache Expiry in Seconds for one or more Caches
* Configuring Default Cache Expiry 
* Configuring Cache Expiry as an absolute Datetime based on CRON format




    