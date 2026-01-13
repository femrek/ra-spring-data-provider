import React from 'react';
import { Admin, Resource, List, Datagrid, TextField, EmailField, Create, SimpleForm, TextInput, Edit, EditButton, DeleteButton } from 'react-admin';
import jsonServerProvider from 'ra-data-json-server';

// Custom data provider to adapt to our Spring Boot API
const dataProvider = jsonServerProvider('http://localhost:8081/api');

// Users List Component
const UserList = () => (
  <List>
    <Datagrid>
      <TextField source="id" />
      <TextField source="name" />
      <EmailField source="email" />
      <TextField source="role" />
      <EditButton />
      <DeleteButton />
    </Datagrid>
  </List>
);

// Users Create Component
const UserCreate = () => (
  <Create redirect="list">
    <SimpleForm>
      <TextInput source="name" required />
      <TextInput source="email" type="email" required />
      <TextInput source="role" />
    </SimpleForm>
  </Create>
);

// Users Edit Component
const UserEdit = () => (
  <Edit redirect="list">
    <SimpleForm>
      <TextInput source="name" required />
      <TextInput source="email" type="email" required />
      <TextInput source="role" />
    </SimpleForm>
  </Edit>
);

const App = () => (
  <Admin dataProvider={dataProvider}>
    <Resource 
      name="users" 
      list={UserList} 
      create={UserCreate} 
      edit={UserEdit}
    />
  </Admin>
);

export default App;
