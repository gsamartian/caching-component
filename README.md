Purpose
-------
The purpose of this component is to encapsulate the caching implementation.
Currently, it wraps the Redis Implementation.
It uses the spring specific dependencies for caching.



It also adds a few custom features as  below which are not available by default in Spring Caching
-------------------------------------------------------------------------------------------------
1.Configuring Cache Expiry in Seconds for one or more Caches
2.Configuring Default Cache Expiry 
3.Configuring Cache Expiry as an absolute Datetime based on CRON format
4.Invalidate all or specific keys from a cache



    