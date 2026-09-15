# Apache Configuration Files

This directory contains Apache httpd configuration files for the bookkeeper.deplake.by server.

## Files:

### `bookkeeper-deplake-by.conf`
- **Server location:** `/etc/apache2/sites-available/bookkeeper-deplake-by.conf`
- **Purpose:** HTTP (port 80) configuration
- **Function:** Redirects all HTTP traffic to HTTPS

### `bookkeeper-deplake-by-le-ssl.conf`
- **Server location:** `/etc/apache2/sites-available/bookkeeper-deplake-by-le-ssl.conf`
- **Purpose:** HTTPS (port 443) configuration
- **Function:** Main configuration handling all traffic
- **SSL:** Uses Let's Encrypt certificates

## Deployment Instructions:

### Initial Setup or Configuration Update:

```bash
# 1. Copy configurations to server
scp configs/bookkeeper-deplake-by.conf user@bookkeeper.deplake.by:/tmp/
scp configs/bookkeeper-deplake-by-le-ssl.conf user@bookkeeper.deplake.by:/tmp/

# 2. On the server, backup existing configs
ssh user@bookkeeper.deplake.by
sudo cp /etc/apache2/sites-available/bookkeeper-deplake-by.conf /etc/apache2/sites-available/bookkeeper-deplake-by.conf.backup
sudo cp /etc/apache2/sites-available/bookkeeper-deplake-by-le-ssl.conf /etc/apache2/sites-available/bookkeeper-deplake-by-le-ssl.conf.backup

# 3. Install new configurations
sudo mv /tmp/bookkeeper-deplake-by.conf /etc/apache2/sites-available/
sudo mv /tmp/bookkeeper-deplake-by-le-ssl.conf /etc/apache2/sites-available/

# 4. Set proper permissions
sudo chown root:root /etc/apache2/sites-available/bookkeeper-deplake-by*.conf
sudo chmod 644 /etc/apache2/sites-available/bookkeeper-deplake-by*.conf

# 5. Test configuration
sudo apache2ctl configtest

# 6. Enable required modules (if not already enabled)
sudo a2enmod rewrite
sudo a2enmod proxy
sudo a2enmod proxy_http
sudo a2enmod ssl

# 7. Enable sites (if not already enabled)
sudo a2ensite bookkeeper-deplake-by.conf
sudo a2ensite bookkeeper-deplake-by-le-ssl.conf

# 8. Restart Apache
sudo systemctl restart apache2

# 9. Verify
curl -s http://localhost | head -10
curl -s https://localhost | head -10
```

## Configuration Overview:

### Routing Rules:

Both HTTP and HTTPS configs use the same routing logic:

| URL Path | Destination | Purpose |
|----------|-------------|---------|
| `/api/*` | Backend container (port 8080) | API endpoints |
| `/token/*` | Backend container (port 8080) | Authentication |
| `/logs/*` | Backend container (port 8080) | Logging endpoints |
| `/mobile-app/*` | Backend container (port 8080) | Mobile APK download |
| `/503.html` | Apache filesystem | Error page (not proxied) |
| Everything else | `/var/www/bookkeeper-deplake-by/` | Angular frontend |

### Key Points:

1. **No `ProxyPass /`**: The configs intentionally do NOT proxy all requests to the backend. Only specific API paths are proxied.

2. **DocumentRoot**: Set to `/var/www/bookkeeper-deplake-by` where the Angular app is deployed.

3. **Angular Routing**: The `<Directory>` block handles Angular's client-side routing by rewriting all non-file requests to `index.html`.

4. **Error Page**: The 503.html error page is excluded from proxying and served directly by Apache.

5. **SSL Certificates**: Managed automatically by Let's Encrypt (certbot). Certificates are stored in `/etc/letsencrypt/live/bookkeeper.deplake.by/`.

## Troubleshooting:

### Backend error page showing instead of frontend:
- Check if `ProxyPass / http://127.0.0.1:8080/` exists in the config - **remove it**
- Verify frontend files exist in `/var/www/bookkeeper-deplake-by/`
- Check Apache error logs: `sudo tail -f /var/log/apache2/error.log`

### 404 on Angular routes:
- Verify rewrite module is enabled: `sudo a2enmod rewrite`
- Check Directory block has `AllowOverride All`
- Restart Apache after changes

### SSL certificate errors:
- Renew certificates: `sudo certbot renew`
- Check certificate paths in `/etc/letsencrypt/live/bookkeeper.deplake.by/`

## Maintenance:

### SSL Certificate Renewal:
Let's Encrypt certificates auto-renew via certbot. No manual intervention needed unless there's an error.

### After Frontend Deployment:
No Apache configuration changes needed. GitHub Actions automatically deploys new frontend files to `/var/www/bookkeeper-deplake-by`.

### After Backend Changes:
No Apache configuration changes needed. Backend Docker container restarts on its own port (8080).
