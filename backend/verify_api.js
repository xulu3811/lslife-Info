fetch('https://mentalhlp.site/api/categories/tree')
  .then(res => res.json())
  .then(json => console.log(JSON.stringify(json, null, 2)));
